package mb.demo.applications.ai.agents.config;

import com.google.adk.agents.LlmAgent;
import com.google.adk.models.springai.SpringAI;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentsConfiguration {

    @Bean
    public LlmAgent apiTestingAgent(ChatModel chatModel) {
        return LlmAgent.builder()
                .name("API-Tester")
                .instruction("You are a QA automation expert. Analyze the provided OpenAPI spec. " +
                        "For every endpoint, generate one valid request. Report all results.")
                .model(new SpringAI(chatModel)) // Connects ADK to Spring AI
                .build();
    }
}
