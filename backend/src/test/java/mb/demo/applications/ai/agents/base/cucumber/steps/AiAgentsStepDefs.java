package mb.demo.applications.ai.agents.base.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.base.cucumber.TestDataHolder;
import mb.demo.applications.ai.agents.utils.FileUtils;
import mb.demo.applications.ai.agents.webapi.model.TestResult;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.io.File;
import java.net.URISyntaxException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class AiAgentsStepDefs extends BaseCucumberStepDefs {

    public AiAgentsStepDefs(TestDataHolder testDataHolder, ObjectMapper objectMapper, RestTestClient restTestClient) {
        super(testDataHolder, objectMapper, restTestClient);
    }

    @When("I call the only endpoint of this service")
    public void iCallTheOnlyEndpointOfThisService() throws URISyntaxException {
        String fullUri = "/api/test/public/openapi";
        File input = FileUtils.getFileFromResources("input-specs/petstore.yaml");
        TestResult response = restTestClient.post()
                .uri(fullUri)
                .body(input)
                .exchange()
                .returnResult(TestResult.class)
                .getResponseBody();
        testDataHolder.setTestResult(response);
    }

    @Then("the response should not be null")
    public void theResponseShouldNotBeNull() {
        assertThat(testDataHolder.getTestResult()).isNotNull();
    }

}
