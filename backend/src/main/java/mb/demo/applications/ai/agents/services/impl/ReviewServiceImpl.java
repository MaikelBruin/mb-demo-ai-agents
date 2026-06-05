package mb.demo.applications.ai.agents.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.services.AgentService;
import mb.demo.applications.ai.agents.services.ReviewService;
import mb.demo.applications.ai.agents.webapi.model.ReviewGitHubPrRequest;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

import java.io.IOException;

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
    public String reviewGithubPullRequest(ReviewGitHubPrRequest request) {
        GHPullRequest pr;
        String repositoryName;
        try {
            repositoryName = request.getUserName() + "/" + request.getRepository();
            GHRepository repo = gitHub.getRepository(repositoryName);
            pr = repo.getPullRequest(request.getPullRequestNumber());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (pr == null) {
            throw new RuntimeException("Could not find PR");
        }
        String prompt;
        try {
            prompt = buildAiPrompt(repositoryName, pr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("prompting agent: '{}'", prompt);
        String agentResult = agentService.doCodeReview(prompt);
        log.info("retrieved result: '{}'", agentResult);
        return agentResult;
    }

    public String buildAiPrompt(String repositoryName, GHPullRequest pr) throws IOException {
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
        return constructPromptText(repositoryName, pr.getNumber(), title, description, author, branchFrom, branchTo, changedFilesCount, codeDiffs.toString());
    }

    private String constructPromptText(String repositoryName, int pullRequestNumber, String title, String desc, String author, String from, String to, int fileCount, String diffs) {
        return """
                You are an expert Senior Software Engineer and Code Reviewer. Please review the following GitHub Pull Request.
               
                ### PULL REQUEST METADATA
                - **Title:** %s
                - **Author:** %s
                - **RepositoryName:** %s
                - **PullRequestNumber:** %s
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
               Be constructive, concise, and precise. If pointing out an issue, explain *why* it's an issue and provide a brief code snippet demonstrating how to fix it.
               Do not include markdown formatting like ```json ... ```.
               Please create a github comment for each review comment that you have.
               """.formatted(title, author, repositoryName, pullRequestNumber, to, from, fileCount, desc, diffs);
    }

}
