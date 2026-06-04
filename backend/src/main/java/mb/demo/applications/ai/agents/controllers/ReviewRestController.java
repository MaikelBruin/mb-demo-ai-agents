package mb.demo.applications.ai.agents.controllers;

import mb.demo.applications.ai.agents.services.ReviewService;
import mb.demo.applications.ai.agents.webapi.api.ReviewingApi;
import mb.demo.applications.ai.agents.webapi.model.ReviewComment;
import mb.demo.applications.ai.agents.webapi.model.ReviewGitHubPrRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReviewRestController implements ReviewingApi {

    private final ReviewService reviewService;

    public ReviewRestController(final ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Override
    @PostMapping(value = "/api/review/public/github/pr")
    public ResponseEntity<List<ReviewComment>> reviewGitHubPr(ReviewGitHubPrRequest reviewGitHubPrRequest) {
        List<ReviewComment> response;
        try {
            response = reviewService.reviewGithubPullRequest(reviewGitHubPrRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(response);
    }

}
