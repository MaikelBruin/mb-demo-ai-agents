package mb.demo.applications.ai.agents.services;

public interface AgentService {

    String getPayload(String operationId);
    String getReport(String testResults);
    String doCodeReview(String prompt);
}
