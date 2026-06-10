package mb.demo.applications.ai.agents.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.services.AgentService;
import mb.demo.applications.ai.agents.services.ReviewService;
import mb.demo.applications.ai.agents.webapi.model.ReviewComment;
import mb.demo.applications.ai.agents.webapi.model.ReviewGitHubPrRequest;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    private final AgentService agentService;
    private final ObjectMapper objectMapper;
    private final GitHub gitHub;

    public ReviewServiceImpl(final AgentService agentService,
                             final ObjectMapper objectMapper,
                             final GitHub gitHub) {
        this.agentService = agentService;
        this.objectMapper = objectMapper;
        this.gitHub = gitHub;
    }

    @Override
    public List<ReviewComment> reviewGithubPullRequest(ReviewGitHubPrRequest request) {
        GHPullRequest pr;
        try {
            GHRepository repo = gitHub.getRepository(request.getUserName() + "/" + request.getRepository());
            pr = repo.getPullRequest(request.getPullRequestNumber());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (pr == null) throw new RuntimeException("Could not find PR");
        String prompt;
        try {
            prompt = buildAiPrompt(pr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("prompting agent: '{}'", prompt);
        String agentResult = agentService.doCodeReview(prompt);
        log.info("retrieved result: '{}'", agentResult);
        List<ReviewComment> result;
        try {
            result = objectMapper.readValue(agentResult, new TypeReference<>() {
            });
            postComments(result, pr);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public void postComments(List<ReviewComment> reviewComments, GHPullRequest pr) {
        for (var comment : reviewComments) {
            postComment(comment, pr);
        }
    }

    public void postComment(ReviewComment comment, GHPullRequest pr) {
        try {
            pr.createReviewComment()
                    .body(comment.getComment())
                    .path(comment.getFileName())
                    .line(comment.getLineNumber())
                    .commitId(pr.getHead().getSha())
                    .create();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String buildAiPrompt(GHPullRequest pr) throws IOException {
        // 1. Fetch Metadata
        String title = pr.getTitle();
        String description = pr.getBody();
        String author = pr.getUser().getLogin();
        String branchFrom = pr.getHead().getLabel();
        String branchTo = pr.getBase().getLabel();
        int changedFilesCount = pr.getChangedFiles();

        // 2. Fetch the Code Changes (The Diff/Hunks)
        StringBuilder codeDiffs = new StringBuilder();
        for (GHPullRequestFileDetail file : pr.listFiles()) {
            codeDiffs.append("File: ").append(file.getFilename()).append("\n");
            codeDiffs.append("Status: ").append(file.getStatus()).append("\n");
            codeDiffs.append("Changes:\n").append(file.getPatch()).append("\n");
            codeDiffs.append("---\n");
        }

        // 3. Construct the final Prompt String
        return constructPromptText(title, description, author, branchFrom, branchTo, changedFilesCount, codeDiffs.toString());
    }

    private String constructPromptText(String title, String desc, String author, String from, String to, int fileCount, String diffs) {
        // String template block (Java 15+)
        return """
                You are an expert Senior Software Engineer and Code Reviewer. Your task is to perform a thorough review of the following GitHub Pull Request.
                
                ### PULL REQUEST METADATA
                - **Title:** %s
                - **Author:** %s
                - **Target Branch:** %s (merging into)
                - **Source Branch:** %s (merging from)
                - **Number of Files Changed:** %d
                
                ### DESCRIPTION / CONTEXT
                %s
                
                ### CODE DIFF
                The code changes are provided below in standard Git diff format. Lines starting with '+' are additions, and lines starting with '-' are deletions.
                
                ```diff
                %s
                ```
                
                ### YOUR INSTRUCTIONS
                Please review the changes above and provide your feedback structured exactly as follows:
                1. **Summary:** A 2-3 sentence overview of what this PR accomplishes.
                2. **Critical Issues:** Any bugs, security vulnerabilities, race conditions, or logical errors.
                3. **Refactoring & Clean Code:** Suggestions for readability, optimization, edge cases, or adherence to best practices.
                4. **Praise:** Point out any exceptionally well-written code or clever implementations.
                
                Be constructive, concise, and precise. If pointing out an issue, explain *why* it's an issue and provide a brief code snippet demonstrating how to fix it.
                Do not include markdown formatting like ```json ... ```.
                """.formatted(title, author, to, from, fileCount, desc, diffs);
    }


}
