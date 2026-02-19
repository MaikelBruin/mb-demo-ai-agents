package mb.demo.applications.ai.agents.base.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.base.cucumber.TestDataHolder;
import org.springframework.boot.resttestclient.TestRestTemplate;

@Slf4j
public abstract class BaseCucumberStepDefs {
    protected final TestRestTemplate testRestTemplate;
    protected final TestDataHolder testDataHolder;
    protected final ObjectMapper objectMapper;

    public BaseCucumberStepDefs(final TestDataHolder testDataHolder, final ObjectMapper objectMapper, final TestRestTemplate testRestTemplate) {
        this.testRestTemplate = testRestTemplate;
        this.testDataHolder = testDataHolder;
        this.objectMapper = objectMapper;
    }
}
