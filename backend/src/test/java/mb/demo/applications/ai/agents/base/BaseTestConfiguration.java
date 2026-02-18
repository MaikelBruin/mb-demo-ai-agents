package mb.demo.applications.ai.agents.base;

import io.cucumber.spring.ScenarioScope;
import mb.demo.applications.ai.agents.base.cucumber.TestDataHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public abstract class BaseTestConfiguration {

    @ScenarioScope
    @Bean
    public TestDataHolder testDataHolder() {
        return new TestDataHolder();
    }

}
