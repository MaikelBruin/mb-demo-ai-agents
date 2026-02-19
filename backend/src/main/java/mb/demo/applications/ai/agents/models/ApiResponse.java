package mb.demo.applications.ai.agents.models;

public record ApiResponse(
        String url,
        int status,
        String responseBody
) {
}