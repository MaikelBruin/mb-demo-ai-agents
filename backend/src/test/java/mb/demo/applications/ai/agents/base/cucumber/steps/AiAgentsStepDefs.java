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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class AiAgentsStepDefs extends BaseCucumberStepDefs {

    private final TestSpecRestController testSpecRestController;
    private final TestSpecService testSpecService;

    public AiAgentsStepDefs(
            final TestDataHolder testDataHolder,
            final ObjectMapper objectMapper,
            final TestSpecRestController testSpecRestController,
            final TestSpecService testSpecService
    ) {
        super(testDataHolder, objectMapper);
        this.testSpecRestController = testSpecRestController;
        this.testSpecService = testSpecService;
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
        assertThat(testDataHolder.getTestResults()).isNotNull();
    }

    @Given("the application has loaded")
    public void theApplicationHasLoaded() {
        assertThat(testSpecRestController).isNotNull();
    }

    @Given("I call the only test spec service method")
    public void iCallTheOnlyTestSpecServiceMethod() throws URISyntaxException, IOException {
        File input = FileUtils.getFileFromResources("input-specs/petstore.yaml");
        List<TestResult> response = testSpecService.testPublicSpec(new MockMultipartFile("spec.yaml", input.getName(), MediaType.APPLICATION_YAML_VALUE, Files.readAllBytes(input.toPath())));
        testDataHolder.setTestResults(response);
    }
}
