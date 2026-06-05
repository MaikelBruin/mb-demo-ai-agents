package mb.demo.applications.ai.agents.config;

import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;
import mb.demo.applications.ai.agents.tools.GitHubTools;
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

    @Bean("codeReviewerAgent")
    public LlmAgent codeReviewerAgent() {
        return LlmAgent.builder()
                       .name("Code-Reviewer")
                       .description("An agent specialized in reviewing code using github pull requests.")
                       .model("gemini-2.5-flash")
                       .instruction("""
                                    Review the incoming GitHub pull request diff for code quality,
                                    adherence to automation patterns (e.g., Page Object Model), security vulnerabilities, hardcoded secrets, and test coverage gaps.
                                    Aside from providing the output in the json response, post all review comments in the PR using the "postReviewCommentTool" tool provided.
                                    When using the "postReviewCommentTool", make sure that the body is a single string that can be passed in an api call.
                                    
                                    RULES:
                                    1. Output ONLY the raw JSON Array string.
                                    2. Do not include markdown formatting like ```json ... ```.
                                    3. Ensure all elements include the properties:
                                                fileName:
                                                  type: string
                                                lineNumber:
                                                  type: integer
                                                  format: int32
                                                comment:
                                                  type: string
                                                priority:
                                                  type: string
                                    .
                                    4. If you are unable to fetch the data from the URL, return a list with a single element in which you explain this.
                                    5. Post all review comments using the provided tool "postReviewCommentTool".
                                    """)
                       .tools(FunctionTool.create(GitHubTools.class, "postReviewCommentTool"))
                       .build();
    }

}
