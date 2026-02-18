package mb.demo.applications.ai.agents.base.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.base.cucumber.TestDataHolder;
import mb.demo.applications.petstore.analyzer.webapi.model.HasAvailableResponse;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class AiAgentsStepDefs extends BaseCucumberStepDefs {

    public AiAgentsStepDefs(CamelContext camelContext, ProducerTemplate producerTemplate, TestDataHolder testDataHolder, ObjectMapper objectMapper, TestRestTemplate testRestTemplate) {
        super(camelContext, producerTemplate, testDataHolder, objectMapper, testRestTemplate);
    }

    @When("I get if there are any rats available")
    public void iGetIfThereAreAnyRatsAvailable() {
        log.info("checking if there are any rats available...");
        String fullUri = "/api/available/rats";
        HasAvailableResponse response = testRestTemplate.getForObject(fullUri, HasAvailableResponse.class);
        testDataHolder.setHasAvailableResponse(response);
    }

    @Then("the has available rats response should not be null")
    public void theHasAvailableRatsResponseShouldNotBeNull() {
        assertThat(testDataHolder.getHasAvailableResponse()).isNotNull();
    }

}
