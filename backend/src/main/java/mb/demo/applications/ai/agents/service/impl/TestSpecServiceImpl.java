package mb.demo.applications.ai.agents.service.impl;

import com.google.adk.agents.LlmAgent;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.models.EndpointTask;
import mb.demo.applications.ai.agents.service.TestSpecService;
import mb.demo.applications.ai.agents.webapi.model.TestResult;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class TestSpecServiceImpl implements TestSpecService {

    private final LlmAgent apiTestAgent;
    private final ProducerTemplate producerTemplate;

    public TestSpecServiceImpl(final LlmAgent apiTestAgent, final ProducerTemplate producerTemplate) {
        this.apiTestAgent = apiTestAgent;
        this.producerTemplate = producerTemplate;
    }

    public List<TestResult> testPublicSpec(MultipartFile file) {
        try {
            // 1. Get string content from the file and parse it
            String specContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<EndpointTask> tasks = parseSpec(specContent);

            // 2. Use the Agent to enrich tasks with payloads and execute via Camel
            return tasks.parallelStream().map(task -> {
                // Let the AI Agent "think" of a valid payload for this specific task
                //FIXME: how to call agent here?
//                String payload = apiTestAgent.ask("Generate a valid JSON for: " + task.operationId());
                String payload = null;
                if (!task.method().equalsIgnoreCase("GET") && !task.method().equalsIgnoreCase("DELETE")) {
                    payload = "{}";
                }

                // Prepare headers for the request
                Map<String, String> headers = new HashMap<>();
                if (payload != null && !payload.isBlank()) {
                    headers.put("Content-Type", "application/json");
                }

                // Execute the call via Camel
                return producerTemplate.requestBody("direct:executeApiCall",
                        new EndpointTask(task.url(), task.method(), task.operationId(), payload, headers),
                        TestResult.class);
            }).toList();
        } catch (IOException e) {
            log.error("Error reading multipart file content", e);
            // Return an empty list or throw a custom exception
            return Collections.emptyList();
        }
    }

    public List<EndpointTask> parseSpec(String specContent) {
        OpenAPI openAPI = new OpenAPIV3Parser().readContents(specContent).getOpenAPI();
        if (openAPI == null || openAPI.getPaths() == null) {
            log.warn("OpenAPI spec could not be parsed or contains no paths.");
            return Collections.emptyList();
        }

        List<EndpointTask> tasks = new ArrayList<>();

        openAPI.getPaths().forEach((path, pathItem) -> {
            // The payload and headers are initially null; they will be enriched later.
            if (pathItem.getGet() != null && pathItem.getGet().getOperationId() != null) {
                tasks.add(new EndpointTask(path, "GET", pathItem.getGet().getOperationId(), null, null));
            }
            if (pathItem.getPost() != null && pathItem.getPost().getOperationId() != null) {
                tasks.add(new EndpointTask(path, "POST", pathItem.getPost().getOperationId(), null, null));
            }
            if (pathItem.getPut() != null && pathItem.getPut().getOperationId() != null) {
                tasks.add(new EndpointTask(path, "PUT", pathItem.getPut().getOperationId(), null, null));
            }
            if (pathItem.getDelete() != null && pathItem.getDelete().getOperationId() != null) {
                tasks.add(new EndpointTask(path, "DELETE", pathItem.getDelete().getOperationId(), null, null));
            }
        });
        return tasks;
    }

}
