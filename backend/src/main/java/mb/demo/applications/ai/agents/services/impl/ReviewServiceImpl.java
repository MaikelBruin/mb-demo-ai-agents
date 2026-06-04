package mb.demo.applications.ai.agents.services.impl;

import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.services.ReviewService;
import mb.demo.applications.ai.agents.webapi.model.ReviewComment;
import mb.demo.applications.ai.agents.webapi.model.ReviewGitHubPrRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    @Override
    public List<ReviewComment> reviewGithubPullRequest(ReviewGitHubPrRequest reviewGitHubPrRequest) {
        return List.of();
    }

}
