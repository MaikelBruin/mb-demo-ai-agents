package mb.demo.applications.ai.agents.integrated.bootstrap;

import io.cucumber.spring.CucumberContextConfiguration;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.AiAgentsApplication;
import mb.demo.applications.ai.agents.integrated.IntegratedTestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(classes = {AiAgentsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegratedTestConfiguration.class)
@ActiveProfiles({"dev", "test", "cucumber"})
@Slf4j
public class IntegratedContainerBootstrapper {
}
