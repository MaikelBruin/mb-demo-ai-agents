package mb.demo.applications.ai.agents.integrated.cucumber.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.platform.suite.api.*;

import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * A configuration for cucumber test setups.
 */
@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "mb.demo.applications.ai.agents.base,mb.demo.applications.ai.agents.integrated")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@Slf4j
public class RunIntegratedCucumberTest {

}
