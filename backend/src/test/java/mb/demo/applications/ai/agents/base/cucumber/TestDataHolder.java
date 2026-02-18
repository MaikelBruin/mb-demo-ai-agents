package mb.demo.applications.ai.agents.base.cucumber;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mb.demo.applications.ai.agents.webapi.model.TestResult;

@NoArgsConstructor
@Getter
@Setter
public class TestDataHolder {

    private TestResult testResult;
    private Exception exception;
}
