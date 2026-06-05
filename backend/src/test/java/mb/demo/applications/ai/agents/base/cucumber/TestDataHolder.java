package mb.demo.applications.ai.agents.base.cucumber;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mb.demo.applications.ai.agents.webapi.model.ReviewComment;
import mb.demo.applications.ai.agents.webapi.model.TestResult;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class TestDataHolder {

    private List<TestResult> testResults;
    private String reviewComments;
    private Exception exception;
}
