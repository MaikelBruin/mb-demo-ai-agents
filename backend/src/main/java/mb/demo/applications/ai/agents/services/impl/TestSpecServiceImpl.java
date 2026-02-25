package mb.demo.applications.ai.agents.services.impl;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.OpenAPIV3Parser;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.models.ApiCall;
import mb.demo.applications.ai.agents.models.ApiCallResponse;
import mb.demo.applications.ai.agents.services.ApiAgentService;
import mb.demo.applications.ai.agents.services.ApiCallService;
import mb.demo.applications.ai.agents.services.TestSpecService;
import mb.demo.applications.ai.agents.utils.OpenApiUtils;
import mb.demo.applications.ai.agents.webapi.model.TestResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class TestSpecServiceImpl implements TestSpecService {

    private final ApiAgentService apiAgentService;
    private final ApiCallService apiCallService;


    public TestSpecServiceImpl(
            final ApiAgentService apiAgentService,
            final ApiCallService apiCallService
    ) {
        this.apiAgentService = apiAgentService;
        this.apiCallService = apiCallService;
    }

    @Override
    public List<TestResult> testPublicSpec(MultipartFile file, @Nullable String token) throws IOException {
        String specContent = new String(file.getBytes(), StandardCharsets.UTF_8);
        OpenAPI openAPI = new OpenAPIV3Parser().readContents(specContent).getOpenAPI();

        //Iterate through endpoints and execute
        return openAPI.getPaths().entrySet().stream().flatMap(pathEntry ->
                pathEntry.getValue().readOperationsMap().entrySet().stream().map(opEntry -> {

                    String path = pathEntry.getKey();
                    String method = opEntry.getKey().name();
                    String payload = null;
                    if (method.equalsIgnoreCase("put") || method.equalsIgnoreCase("patch") || method.equalsIgnoreCase("post")) {
                        //Ask Agent for a valid payload based on the operation's schema
                        String prompt = String.format("Generate one valid JSON object for %s %s. Spec: %s",
                                method, path, opEntry.getValue().getRequestBody());
                        log.debug("prompting agent: '{}'", prompt);
                        payload = apiAgentService.getPayload(prompt);
                        log.info("retrieved payload: '{}'", payload);
                    }

                    // Execute Call
                    String url = openAPI.getServers().getFirst().getUrl() + path;
                    log.info("{}: '{}'", method, url);
                    List<Parameter> parameters = opEntry.getValue().getParameters();
                    url = OpenApiUtils.replacePathParameters(parameters, url);
                    url = OpenApiUtils.addRequiredQueryParameters(parameters, url);

                    ApiCallResponse response = apiCallService.executeCall(new ApiCall(url, method, payload), token);
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

}
