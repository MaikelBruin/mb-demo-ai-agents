package mb.demo.applications.ai.agents.models;

public record ApiCall(
        String url,
        String method,
        String payload
) {
}
