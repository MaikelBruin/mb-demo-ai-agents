package mb.demo.applications.ai.agents.services;

import mb.demo.applications.ai.agents.webapi.model.ReviewGitHubPrRequest;

public interface ReviewService {
    String reviewGithubPullRequest(ReviewGitHubPrRequest reviewGitHubPrRequest);
}
