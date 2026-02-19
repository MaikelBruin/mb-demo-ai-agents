package mb.demo.applications.ai.agents.config;

import com.google.adk.agents.LlmAgent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentsConfiguration {

    @Bean
    public LlmAgent apiTestingAgent() {
        return LlmAgent.builder()
                .name("API-Contract-Tester")
                .description("An agent specialized in generating valid test data based on OpenAPI specifications.")
                .model("gemini-2.5-flash") // Use the model best for JSON generation
                .instruction("""
                    You are a QA Automation Engineer. Your task is to analyze OpenAPI operation schemas
                    and generate a single, high-quality, valid JSON object that matches the schema exactly.
                    
                    RULES:
                    1. Output ONLY the raw JSON string.
                    2. Do not include markdown formatting like ```json ... ```.
                    3. Ensure all required fields are present with realistic dummy data.
                    4. Adhere strictly to types (e.g., if a field is an integer, do not provide a string).
                    """)
                .build();
    }
}
