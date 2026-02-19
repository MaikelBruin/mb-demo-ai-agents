package mb.demo.applications.ai.agents.base.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.base.cucumber.TestDataHolder;
import org.springframework.test.web.servlet.client.RestTestClient;

@Slf4j
public abstract class BaseCucumberStepDefs {
    protected final TestDataHolder testDataHolder;
    protected final ObjectMapper objectMapper;
    protected RestTestClient restTestClient;

    public BaseCucumberStepDefs(final TestDataHolder testDataHolder, final ObjectMapper objectMapper) {
        this.testDataHolder = testDataHolder;
        this.objectMapper = objectMapper;
    }
}
