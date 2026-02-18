package mb.demo.applications.ai.agents;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info=@Info(title="AI Agents API", description = "API that lets an AI agent test another API"))
@SpringBootApplication
public class AiAgentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiAgentsApplication.class, args);
	}

}
