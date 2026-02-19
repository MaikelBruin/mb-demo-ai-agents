package mb.demo.applications.ai.agents.models;

public record ApiCallResponse(
        String url,
        int status,
        String responseBody
) {
}