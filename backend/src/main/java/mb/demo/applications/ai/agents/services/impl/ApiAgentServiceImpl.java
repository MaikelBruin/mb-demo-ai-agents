package mb.demo.applications.ai.agents.services.impl;

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import mb.demo.applications.ai.agents.services.ApiAgentService;
import org.springframework.stereotype.Service;

@Service
public class ApiAgentServiceImpl implements ApiAgentService {

    private final LlmAgent apiTestAgent;

    public ApiAgentServiceImpl(final LlmAgent apiTestAgent) {
        this.apiTestAgent = apiTestAgent;
    }

    public String getPayload(String operationSchema) {
        // Create a new runner for each request to ensure a clean state.
        RunConfig runConfig = RunConfig.builder().build();
        InMemoryRunner runner = new InMemoryRunner(apiTestAgent);

        // 1. Create a unique session for this specific request
        Session session = runner.sessionService()
                .createSession(runner.appName(), "user-123")
                .blockingGet();

        // 2. Ask the agent to "reason" over the schema
        String prompt = "Generate JSON for this schema: " + operationSchema;
        Content content = Content.fromParts(Part.fromText(prompt));
        Event response = runner.runAsync(session.userId(), session.id(), content, runConfig)
                .blockingFirst();

        // 3. Extract the text (which we instructed to be raw JSON)
        return response.stringifyContent();
    }
}
