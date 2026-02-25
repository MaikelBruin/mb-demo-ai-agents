package mb.demo.applications.ai.agents.service.impl;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.OpenAPIV3Parser;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.models.ApiCall;
import mb.demo.applications.ai.agents.models.ApiCallResponse;
import mb.demo.applications.ai.agents.service.ApiAgentService;
import mb.demo.applications.ai.agents.service.TestSpecService;
import mb.demo.applications.ai.agents.webapi.model.TestResult;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
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
    public List<TestResult> testPublicSpec(MultipartFile file, @Nullable String token) throws IOException {
        String specContent = new String(file.getBytes(), StandardCharsets.UTF_8);
        // 1. Parse Spec
        OpenAPI openAPI = new OpenAPIV3Parser().readContents(specContent).getOpenAPI();

        // 2. Iterate through endpoints and execute
        return openAPI.getPaths().entrySet().stream().flatMap(pathEntry ->
                pathEntry.getValue().readOperationsMap().entrySet().stream().map(opEntry -> {

                    String path = pathEntry.getKey();
                    String method = opEntry.getKey().name();
                    String payload = null;
                    if (method.equalsIgnoreCase("put") || method.equalsIgnoreCase("patch") || method.equalsIgnoreCase("post")) {
                        // 3. Ask Agent for a valid payload based on the operation's schema
                        String prompt = String.format("Generate one valid JSON object for %s %s. Spec: %s",
                                method, path, opEntry.getValue().getRequestBody());
                        log.debug("prompting agent: '{}'", prompt);
                        payload = apiAgentService.getPayload(prompt);
                        log.info("retrieved payload: '{}'", payload);
                    }

                    // 4. Execute Call
                    String url = openAPI.getServers().getFirst().getUrl() + path;
                    log.info("{}: '{}'", method, url);
                    //replace path params
                    List<Parameter> parameters = opEntry.getValue().getParameters();
                    url = replacePathParameters(parameters, url);
                    //replace query params
                    url = addRequiredQueryParameters(parameters, url);

                    ApiCallResponse response = executeCall(new ApiCall(url, method, payload), token);
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

    private static String replacePathParameters(List<Parameter> parameters, String url) {
        if (parameters != null) {
            List<Parameter> pathParams = parameters
                    .stream()
                    .filter(parameter -> parameter.getIn().equalsIgnoreCase("path"))
                    .toList();
            if (!pathParams.isEmpty()) {
                for (Parameter parameter : pathParams) {
                    String name = parameter.getName();
                    Object exampleValue = getExampleValue(parameter);
                    String paramInPath = "{" + name + "}";
                    url = url.replace(paramInPath, exampleValue.toString());
                }
                log.info("fixed url for path params: '{}'", url);
            }
        }
        return url;
    }

    private static String addRequiredQueryParameters(List<Parameter> parameters, String url) {
        if (parameters != null) {
            List<Parameter> requiredQueryParams = parameters
                    .stream()
                    .filter(parameter -> parameter.getIn().equalsIgnoreCase("query")
                            && parameter.getRequired())
                    .toList();
            if (!requiredQueryParams.isEmpty()) {
                url = url.concat("?");
                for (Parameter parameter : requiredQueryParams) {
                    String name = parameter.getName();
                    Object exampleValue = getExampleValue(parameter);
                    url = url.concat(name + "=" + exampleValue + "&");
                }
                url = url.substring(0, url.length() - 1);
                log.info("added required query params: '{}'", url);
            }
        }
        return url;
    }

    @NotNull
    private static Object getExampleValue(Parameter parameter) {
        Object exampleValue = parameter.getExample() != null
                ? parameter.getExample()
                : parameter.getSchema().getDefault();
        //check for array
        if (parameter.getSchema().getItems() != null && parameter.getSchema().getItems().getType() != null) {
            Object itemExample = parameter.getSchema().getItems().getExample();
            if (itemExample == null) {
                String type = parameter.getSchema().getItems().getType();
                if (type.equalsIgnoreCase("string")) exampleValue = "[someString]";
                else exampleValue = "[123]";
            }
        }

        //set default value if no examples are listed in spec
        if (exampleValue == null) {
            exampleValue = switch (parameter.getSchema().getType().toUpperCase()) {
                case "STRING" -> "someValue";
                case "NUMBER", "INTEGER" -> 1;
                case "BOOLEAN" -> true;
                case "OBJECT" -> new Object();
                default -> exampleValue;
            };
        }

        if (exampleValue == null)
            throw new RuntimeException("Could not find example value for parameter: " + parameter.getName());
        return exampleValue;
    }

    private ApiCallResponse executeCall(ApiCall call, @Nullable String token) {
        try {
            String methodString = call.method().toUpperCase();
            HttpMethod method = HttpMethod.valueOf(methodString);
            RestClient.RequestBodySpec requestBodySpec = restClient.method(method)
                    .uri(call.url())
                    .accept(MediaType.APPLICATION_JSON);
            switch (methodString) {
                case "POST", "PUT", "PATCH" -> requestBodySpec = requestBodySpec
                        .body(call.payload())
                        .contentType(MediaType.APPLICATION_JSON);
            }

            if (token != null) {
                requestBodySpec = requestBodySpec.header("Authorization", token);
            }

            ResponseEntity<String> response = requestBodySpec
                    .retrieve()
                    .onStatus(new DefaultResponseErrorHandler())
                    .toEntity(String.class);
            return new ApiCallResponse(call.url(), response.getStatusCode().value(), response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return new ApiCallResponse(call.url(), e.getStatusCode().value(), e.getResponseBodyAsString());
        }
    }


}
