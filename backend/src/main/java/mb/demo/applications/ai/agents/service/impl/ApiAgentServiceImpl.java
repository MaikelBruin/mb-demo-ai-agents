package mb.demo.applications.ai.agents.service.impl;

import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import mb.demo.applications.ai.agents.service.ApiAgentService;
import org.springframework.stereotype.Service;

@Service
public class ApiAgentServiceImpl implements ApiAgentService {

    private final InMemoryRunner runner;

    public ApiAgentServiceImpl(final LlmAgent apiTestAgent) {
        this.runner = new InMemoryRunner(apiTestAgent);
    }

    public String getPayload(String operationSchema) {
        // 1. Create a unique session for this specific request
        Session session = runner.sessionService()
                .createSession("api-scanner", "user-123")
                .blockingGet();

        // 2. Ask the agent to "reason" over the schema
        String prompt = "Generate JSON for this schema: " + operationSchema;
        Content content = Content.fromParts(Part.fromText(prompt));
        Event response = runner.runAsync(session.userId(), session.id(), content)
                .blockingFirst();

        // 3. Extract the text (which we instructed to be raw JSON)
        return response.stringifyContent();
    }
}
