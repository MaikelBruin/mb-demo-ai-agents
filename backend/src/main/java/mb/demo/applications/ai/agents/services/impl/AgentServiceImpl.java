package mb.demo.applications.ai.agents.services.impl;

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.plugins.LoggingPlugin;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import mb.demo.applications.ai.agents.services.AgentService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentServiceImpl implements AgentService {

    private final LlmAgent payloadGeneratorAgent;
    private final LlmAgent reportGeneratorAgent;
    private final LlmAgent codeReviewerAgent;

    public AgentServiceImpl(
            @Qualifier("payloadGeneratorAgent") final LlmAgent payloadGeneratorAgent,
            @Qualifier("reportGeneratorAgent") final LlmAgent reportGeneratorAgent,
            @Qualifier("codeReviewerAgent") final LlmAgent codeReviewerAgent
    ) {
        this.payloadGeneratorAgent = payloadGeneratorAgent;
        this.reportGeneratorAgent = reportGeneratorAgent;
        this.codeReviewerAgent = codeReviewerAgent;
    }

    public String getPayload(String operationSchema) {
        RunConfig runConfig = RunConfig.builder().build();
        InMemoryRunner runner = new InMemoryRunner(payloadGeneratorAgent, "payloadGeneratorAgent", List.of(new LoggingPlugin()));

        Session session = runner.sessionService()
                .createSession(runner.appName(), "user-123")
                .blockingGet();

        String prompt = "Generate JSON for this schema: " + operationSchema;
        Content content = Content.fromParts(Part.fromText(prompt));
        Event response = runner.runAsync(session.userId(), session.id(), content, runConfig)
                .blockingFirst();

        return response.stringifyContent();
    }

    public String getReport(String testResults) {
        RunConfig runConfig = RunConfig.builder().build();
        InMemoryRunner runner = new InMemoryRunner(reportGeneratorAgent, "reportGeneratorAgent", List.of(new LoggingPlugin()));

        Session session = runner.sessionService()
                .createSession(runner.appName(), "user-123")
                .blockingGet();
        Content content = Content.fromParts(Part.fromText(testResults));
        Event response = runner.runAsync(session.userId(), session.id(), content, runConfig)
                .blockingFirst();
        return response.stringifyContent();
    }

    public String doCodeReview(String prompt) {
        RunConfig runConfig = RunConfig.builder().build();
        InMemoryRunner runner = new InMemoryRunner(codeReviewerAgent, "codeReviewerAgent", List.of(new LoggingPlugin()));

        Session session = runner.sessionService()
                                .createSession(runner.appName(), "user-123")
                                .blockingGet();
        Content content = Content.fromParts(Part.fromText(prompt));
        Event response = runner.runAsync(session.userId(), session.id(), content, runConfig)
                               .blockingFirst();
        return response.stringifyContent();
    }
}
