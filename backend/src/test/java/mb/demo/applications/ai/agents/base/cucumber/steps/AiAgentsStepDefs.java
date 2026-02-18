package mb.demo.applications.ai.agents.base.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.base.cucumber.TestDataHolder;
import mb.demo.applications.ai.agents.webapi.model.HasAvailableResponse;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class AiAgentsStepDefs extends BaseCucumberStepDefs {

    public AiAgentsStepDefs(CamelContext camelContext, ProducerTemplate producerTemplate, TestDataHolder testDataHolder, ObjectMapper objectMapper, TestRestTemplate testRestTemplate) {
        super(camelContext, producerTemplate, testDataHolder, objectMapper, testRestTemplate);
    }

    @When("I call the only endpoint of this service")
    public void iCallTheOnlyEndpointOfThisService() {
        String fullUri = "/api/available/rats";
        HasAvailableResponse response = testRestTemplate.getForObject(fullUri, HasAvailableResponse.class);
        testDataHolder.setHasAvailableResponse(response);
    }

    @Then("the response should not be null")
    public void theResponseShouldNotBeNull() {
        assertThat(testDataHolder.getHasAvailableResponse()).isNotNull();
    }

}
