package mb.demo.applications.ai.agents.service.impl;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.models.ApiCall;
import mb.demo.applications.ai.agents.models.ApiCallResponse;
import mb.demo.applications.ai.agents.service.ApiAgentService;
import mb.demo.applications.ai.agents.service.TestSpecService;
import mb.demo.applications.ai.agents.webapi.model.TestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class TestSpecServiceImpl implements TestSpecService {

    private final ApiAgentService apiAgentService;
    private final RestClient restClient;


    public TestSpecServiceImpl(final ApiAgentService apiAgentService) {
        this.apiAgentService = apiAgentService;
        this.restClient = RestClient.create();
    }

    @Override
    public List<TestResult> testPublicSpec(MultipartFile file) throws IOException {
        String specContent = new String(file.getBytes(), StandardCharsets.UTF_8);
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
                    String url = openAPI.getServers().getFirst().getUrl() + path;
                    ApiCallResponse response =  executeCall(new ApiCall(url, method, payload));
                    return new TestResult()
                            .url(url)
                            .method(method)
                            .operationId(opEntry.getValue().getOperationId())
                            .payload(payload)
                            .statusCode(response.status())
                            .responseBody(response.responseBody());
                })
        ).toList();
    }

    private ApiCallResponse executeCall(ApiCall call) {
        try {
            ResponseEntity<String> response = restClient.method(HttpMethod.valueOf(call.method()))
                    .uri(call.url())
                    .body(call.payload())
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(String.class);

            return new ApiCallResponse(call.url(), response.getStatusCode().value(), response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return new ApiCallResponse(call.url(), e.getStatusCode().value(), e.getResponseBodyAsString());
        }
    }


}
