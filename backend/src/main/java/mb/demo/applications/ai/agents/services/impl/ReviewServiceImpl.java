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
            codeDiffs.append("Changes:\n").append(annotatePatchWithLineNumbers(file.getPatch())).append("\n");
            codeDiffs.append("---\n");
        }

        // 3. Construct the final Prompt String
        return constructPromptText(title, description, author, branchFrom, branchTo, changedFilesCount, codeDiffs.toString());
    }

    private String annotatePatchWithLineNumbers(String patch) {
        if (patch == null || patch.isBlank()) return "";
        StringBuilder annotated = new StringBuilder();
        int newLine = 0;

        for (String line : patch.split("\n")) {
            if (line.startsWith("@@")) {
                annotated.append(line).append("\n");
                try {
                    // Extract the starting line number for the new file (from @@ -x,y +startLine,count @@)
                    int plusIdx = line.indexOf('+');
                    int commaIdx = line.indexOf(',', plusIdx);
                    int spaceIdx = line.indexOf(' ', plusIdx);
                    int endIdx = (commaIdx != -1 && commaIdx < spaceIdx) ? commaIdx : spaceIdx;
                    newLine = Integer.parseInt(line.substring(plusIdx + 1, endIdx));
                } catch (Exception e) {
                    log.warn("Failed to parse hunk header: {}", line);
                }
            } else if (line.startsWith("-")) {
                annotated.append(line).append("\n"); // Deletions remain untouched
            } else if (line.startsWith("+")) {
                annotated.append(String.format("+ [Line %d] %s\n", newLine++, line.substring(1)));
            } else if (line.startsWith(" ")) {
                annotated.append(String.format("  [Line %d] %s\n", newLine++, line.substring(1)));
            } else {
                annotated.append(line).append("\n");
            }
        }
        return annotated.toString();
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
                The additions and context lines have been annotated with their absolute line numbers like [Line X].
                
                ```diff
                %s
                ```
                
                ### YOUR INSTRUCTIONS
                Please review the changes above. You must return a STRICT JSON array of objects representing your review comments.
                Each object must have exactly the following structure:
                - "fileName": the exact name of the file
                - "lineNumber": the exact integer line number from the [Line X] annotations where the comment applies
                - "comment": your detailed review feedback (incorporating critical issues, refactoring suggestions, or praise)
                
                Do not include markdown formatting like ```json ... ```. Only return the raw JSON array.
                """.formatted(title, author, to, from, fileCount, desc, diffs);
    }


}
