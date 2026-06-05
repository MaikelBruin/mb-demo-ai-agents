package mb.demo.applications.ai.agents.tools;

import com.google.adk.tools.Annotations;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class GitHubTools {

    private static GitHub GITHUB_CLIENT = null;

    public static void setGithubClient(GitHub gitHub) {
        GITHUB_CLIENT = gitHub;
    }

    @Annotations.Schema(name = "post_review_comment", description = "posts a review comment in a github PR")
    public static Map<String, String> postReviewCommentTool(
            @Annotations.Schema(name = "repositoryName",
                    description = "The name of the repository") String repositoryName,
            @Annotations.Schema(name = "pullRequestNumber",
                    description = "The number of the pull request") int pullRequestNumber,
            @Annotations.Schema(name = "filePath",
                    description = "The path of the file to comment on") String filePath,
            @Annotations.Schema(name = "lineNumber",
                    description = "The line number to comment on") int lineNumber,
            @Annotations.Schema(name = "body",
                    description = "The content of the comment") String body) {
        log.info("using tool...");
        try {
            postReviewComment(repositoryName, pullRequestNumber, filePath, lineNumber, body);
            log.info("tool used!");
            return Map.of("status", "success");
        } catch (IOException e) {
            log.info("tool failed due to error: {}", e.getMessage());
            return Map.of("status", "error");
        }
    }

    private static void postReviewComment(String repositoryName,
                                          int pullRequestNumber,
                                          String filePath,
                                          int lineNumber,
                                          String body) throws IOException {
        log.info("posting comment...");
        GHRepository repo = GITHUB_CLIENT.getRepository(repositoryName);
        GHPullRequest pr = repo.getPullRequest(pullRequestNumber);
        pr.createReviewComment().body(body).line(lineNumber).path(filePath).commitId("31696112859312a83678a8547c90859186a837d8").create();
        log.info("comment posted!");
    }

}
