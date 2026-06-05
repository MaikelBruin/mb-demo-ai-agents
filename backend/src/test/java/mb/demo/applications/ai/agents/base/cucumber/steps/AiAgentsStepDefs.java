package mb.demo.applications.ai.agents.base.cucumber.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.base.cucumber.TestDataHolder;
import mb.demo.applications.ai.agents.controllers.TestSpecRestController;
import mb.demo.applications.ai.agents.services.ReviewService;
import mb.demo.applications.ai.agents.services.TestSpecService;
import mb.demo.applications.ai.agents.services.impl.AgentServiceImpl;
import mb.demo.applications.ai.agents.utils.FileUtils;
import mb.demo.applications.ai.agents.webapi.model.ReviewComment;
import mb.demo.applications.ai.agents.webapi.model.ReviewGitHubPrRequest;
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

    private final ReviewService reviewService;

    private final AgentServiceImpl agentService;

    private final ObjectMapper objectMapper;

    public AiAgentsStepDefs(
            final TestDataHolder testDataHolder,
            final ObjectMapper objectMapper,
            final TestSpecRestController testSpecRestController,
            final TestSpecService testSpecService,
            final ReviewService reviewService,
            final AgentServiceImpl agentService
    ) {
        super(testDataHolder, objectMapper);
        this.testSpecRestController = testSpecRestController;
        this.testSpecService = testSpecService;
        this.objectMapper = objectMapper;
        this.reviewService = reviewService;
        this.agentService = agentService;
    }

    @Given("I test the openapi spec {string} with help of my agent")
    public void iTestTheOpenapiSpecWithHelpOfMyAgent(String fileName) throws URISyntaxException, IOException {
        File input = FileUtils.getFileFromResources("input-specs/" + fileName);
        MockMultipartFile file = new MockMultipartFile("spec.yaml", input.getName(), MediaType.APPLICATION_YAML_VALUE, Files.readAllBytes(input.toPath()));
        List<TestResult> response = testSpecService.testPublicSpec(file, null);
        testDataHolder.setTestResults(response);
    }

    @Then("there should be test results")
    public void thereShouldBeTestResults() throws JsonProcessingException {
        List<TestResult> actualResults = testDataHolder.getTestResults();
        log.info("test results: {}", objectMapper.writeValueAsString(actualResults));
        Assertions.assertThat(actualResults).isNotNull();
        Assertions.assertThat(actualResults).isNotEmpty();
    }

    @Given("I test the secret openapi spec {string} with help of my agent")
    public void iTestTheSecretOpenapiSpecWithHelpOfMyAgent(String fileName) throws URISyntaxException, IOException {
        File input = FileUtils.getFileFromResources("input-specs/secret/" + fileName);
        MockMultipartFile file = new MockMultipartFile("spec.yaml", input.getName(), MediaType.APPLICATION_YAML_VALUE, Files.readAllBytes(input.toPath()));
        String token = System.getenv("SECRET_API_TOKEN");
        List<TestResult> response = testSpecService.testPublicSpec(file, token);
        testDataHolder.setTestResults(response);
    }

    @And("I create an html report")
    public void iCreateAnHtmlReport() throws IOException {
        File testOutputDir = new File("target" + File.separator + "test-output");
        if (!testOutputDir.exists()) {
            Files.createDirectories(testOutputDir.toPath());
        }
        String htmlString = agentService.getReport(objectMapper.writeValueAsString(testDataHolder.getTestResults()));
        String dataFilePath = "target" + File.separator + "test-output" + File.separator + System.currentTimeMillis() + "-report.html";
        FileUtils.writeToFile(dataFilePath, htmlString);
    }

    @Given("I review a github pull request")
    public void iReviewAGithubPullRequest() {
        ReviewGitHubPrRequest reviewGitHubPrRequest = new ReviewGitHubPrRequest()
                                                              .pullRequestNumber(11)
                                                              .repository("mb-testframeworks-java")
                                                              .userName("MaikelBruin");
        String result = reviewService.reviewGithubPullRequest(reviewGitHubPrRequest);
        testDataHolder.setReviewComments(result);
    }

    @Then("there should be review comments")
    public void thereShouldBeReviewComments() {
        Assertions.assertThat(testDataHolder.getReviewComments()).isNotEmpty();
    }

}
