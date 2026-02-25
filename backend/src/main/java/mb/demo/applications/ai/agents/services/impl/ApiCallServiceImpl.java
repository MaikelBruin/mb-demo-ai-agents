package mb.demo.applications.ai.agents.services.impl;

import mb.demo.applications.ai.agents.models.ApiCall;
import mb.demo.applications.ai.agents.models.ApiCallResponse;
import mb.demo.applications.ai.agents.services.ApiCallService;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Service
public class ApiCallServiceImpl implements ApiCallService {

    private final RestClient restClient;

    public ApiCallServiceImpl() {
        restClient = RestClient.create();
    }

    @Override
    public ApiCallResponse executeCall(ApiCall call, @Nullable String token) {
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
