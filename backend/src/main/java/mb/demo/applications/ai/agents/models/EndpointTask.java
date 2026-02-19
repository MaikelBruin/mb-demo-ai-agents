package mb.demo.applications.ai.agents.models;

import java.util.Map;

public record EndpointTask(
        String url,
        String method,
        String operationId,
        String payload,
        Map<String, String> headers,
        int statusCode,
        String responseBody
) {
}
