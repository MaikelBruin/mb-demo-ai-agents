package mb.demo.applications.ai.agents.config;

import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.tools.GitHubTools;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
public class GitHubClientConfiguration {

    @Value("${mb-demo-ai-agents.github.user}")
    private String user;

    @Value("${mb-demo-ai-agents.github.token}")
    private String gitHubToken;

    @Bean
    public GitHub gitHub() throws IOException {
        GitHub gitHub = new GitHubBuilder().withOAuthToken(gitHubToken).build();
        GitHubTools.setGithubClient(gitHub);
        log.info("logged into github as {}", gitHub.getMyself().getLogin());
        return gitHub;
    }
}
