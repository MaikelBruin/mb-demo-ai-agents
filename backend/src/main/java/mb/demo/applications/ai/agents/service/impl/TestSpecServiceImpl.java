package mb.demo.applications.ai.agents.service.impl;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.models.ApiCall;
import mb.demo.applications.ai.agents.models.ApiResponse;
import mb.demo.applications.ai.agents.models.EndpointTask;
import mb.demo.applications.ai.agents.service.ApiAgentService;
import mb.demo.applications.ai.agents.service.TestSpecService;
import mb.demo.applications.ai.agents.webapi.model.TestResult;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class TestSpecServiceImpl implements TestSpecService {

    private final ApiAgentService apiAgentService;
    private final RestClient restClient;

    public TestSpecServiceImpl(final ApiAgentService apiAgentService, final RestClient restClient) {
        this.apiAgentService = apiAgentService;
        this.restClient = restClient;
    }

    public List<ApiResponse> processSpec(String specContent) {
        // 1. Parse Spec
        OpenAPI openAPI = new OpenAPIV3Parser().readContents(specContent).getOpenAPI();

        // 2. Iterate through endpoints and execute
        return openAPI.getPaths().entrySet().stream().flatMap(pathEntry ->
                pathEntry.getValue().readOperationsMap().entrySet().stream().map(opEntry -> {

                    String path = pathEntry.getKey();
                    String method = opEntry.getKey().name();

                    // 3. Ask Agent for a valid payload based on the operation's schema
                    String prompt = String.format("Generate one valid JSON object for %s %s. Spec: %s",
                            method, path, opEntry.getValue().getRequestBody());

                    String payload = apiAgentService.getPayload(prompt);

                    // 4. Execute Call
                    return executeCall(new ApiCall(openAPI.getServers().get(0).getUrl() + path, method, payload));
                })
        ).toList();
    }

    private ApiResponse executeCall(ApiCall call) {
        try {
            var response = restClient.method(HttpMethod.valueOf(call.method()))
                    .uri(call.url())
                    .body(call.payload())
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(String.class);

            return new ApiResponse(call.url(), response.getStatusCode().value(), call.payload(), response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return new ApiResponse(call.url(), e.getStatusCode().value(), call.payload(), e.getResponseBodyAsString());
        }
    }

    public List<TestResult> testPublicSpec(MultipartFile file) {
        try {
            // 1. Get string content from the file and parse it
            String specContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<EndpointTask> tasks = parseSpec(specContent);

            // 2. Use the Agent to enrich tasks with payloads and execute via Camel
            return tasks.parallelStream().map(task -> {
                // Let the AI Agent "think" of a valid payload for this specific task
                String payload = null;
                if (!task.method().equalsIgnoreCase("GET") && !task.method().equalsIgnoreCase("DELETE")) {
                    payload = apiAgentService.getPayload(task.operationId());
                }

                // Prepare headers for the request
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

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
