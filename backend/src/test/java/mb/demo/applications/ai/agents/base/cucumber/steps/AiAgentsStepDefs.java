package mb.demo.applications.ai.agents.base.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.base.cucumber.TestDataHolder;
import mb.demo.applications.ai.agents.controllers.TestSpecRestController;
import mb.demo.applications.ai.agents.service.TestSpecService;
import mb.demo.applications.ai.agents.utils.FileUtils;
import mb.demo.applications.ai.agents.webapi.model.TestResult;
import org.assertj.core.api.Assertions;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;

@Slf4j
public class AiAgentsStepDefs extends BaseCucumberStepDefs {

    private final TestSpecRestController testSpecRestController;
    private final TestSpecService testSpecService;
    private final ObjectMapper objectMapper;

    public AiAgentsStepDefs(
            final TestDataHolder testDataHolder,
            final ObjectMapper objectMapper,
            final TestSpecRestController testSpecRestController,
            final TestSpecService testSpecService,
            final ObjectMapper objectMapper1
    ) {
        super(testDataHolder, objectMapper);
        this.testSpecRestController = testSpecRestController;
        this.testSpecService = testSpecService;
        this.objectMapper = objectMapper1;
    }

    @When("I call the only endpoint of this service")
    public void iCallTheOnlyEndpointOfThisService() throws URISyntaxException {
        String fullUri = "/api/test/public/openapi";
        File input = FileUtils.getFileFromResources("input-specs/petstore.yaml");
        restTestClient = RestTestClient.bindToServer().baseUrl("http://localhost:8080").build();
        restTestClient = RestTestClient.bindToController(testSpecRestController).build();
        List response = restTestClient.post()
                .uri(fullUri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(input)
                .exchange()
                .returnResult(List.class)
                .getResponseBody();

        testDataHolder.setTestResults(response);
    }

    @Then("the response should not be null")
    public void theResponseShouldNotBeNull() {
        Assertions.assertThat(testDataHolder.getTestResults()).isNotNull();
    }

    @Given("I test the openapi spec {string} with help of my agent")
    public void iTestTheOpenapiSpecWithHelpOfMyAgent(String fileName) throws URISyntaxException, IOException {
        File input = FileUtils.getFileFromResources("input-specs/" + fileName);
        List<TestResult> response = testSpecService.testPublicSpec(new MockMultipartFile("spec.yaml", input.getName(), MediaType.APPLICATION_YAML_VALUE, Files.readAllBytes(input.toPath())));
        testDataHolder.setTestResults(response);
        log.info("test results: {}", objectMapper.writeValueAsString(response));
    }

    @Then("there should be test results")
    public void thereShouldBeTestResults() {
        Assertions.assertThat(testDataHolder.getTestResults()).isNotNull();
        Assertions.assertThat(testDataHolder.getTestResults()).isNotEmpty();
    }
}
