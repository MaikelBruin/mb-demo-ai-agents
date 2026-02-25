package mb.demo.applications.ai.agents.config;

import com.google.adk.agents.LlmAgent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentsConfiguration {

    @Bean("payloadGeneratorAgent")
    public LlmAgent payloadGeneratorAgent() {
        return LlmAgent.builder()
                .name("API-Payload-Generator")
                .description("An agent specialized in generating valid test data based on OpenAPI specifications.")
                .model("gemini-2.5-flash")
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

    @Bean("reportGeneratorAgent")
    public LlmAgent reportGeneratorAgent() {
        return LlmAgent.builder()
                .name("Test-Report-Generator")
                .description("An agent specialized in generating valid HTML reports based on test results.")
                .model("gemini-2.5-flash")
                .instruction("""
                    You are a QA Automation Engineer. Your task is to analyze Test Results
                    and generate a single, high-quality, valid HTML page including the results.
                    
                    RULES:
                    1. Output ONLY the raw HTML string.
                    2. Do not include markdown formatting like ```html ... ```.
                    3. Highlight test result objects in red if they failed with statusCode other than 2xx.
                    4. Each test must be clickable to expand the details of the test.
                    """)
                .build();
    }
}
