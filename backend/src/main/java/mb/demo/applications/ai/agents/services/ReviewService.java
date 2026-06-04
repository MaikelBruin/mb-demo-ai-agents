package mb.demo.applications.ai.agents.services;

import mb.demo.applications.ai.agents.webapi.model.ReviewComment;
import mb.demo.applications.ai.agents.webapi.model.ReviewGitHubPrRequest;

import java.util.List;

public interface ReviewService {
    List<ReviewComment> reviewGithubPullRequest(ReviewGitHubPrRequest reviewGitHubPrRequest);
}
