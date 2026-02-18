package mb.demo.applications.ai.agents.routes;

import mb.demo.applications.ai.agents.webapi.model.TestResult;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;

public class ApiRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        // Global Error Handling for the route
        onException(HttpOperationFailedException.class)
                .handled(true)
                .to("direct:notifyGoogleChat")
                .process(exchange -> {
                    // Return a failure TestResult to the service
                    exchange.getIn().setBody(new TestResult().status(400).message("Failure"));
                });

        from("direct:executeApiCall")
                .setHeader(Exchange.HTTP_METHOD, simple("${body.method}"))
                .setBody(simple("${body.payload}"))
                .toD("${body.url}") // Dynamic URI execution
                .process(exchange -> {
                    exchange.getIn().setBody(new TestResult().status(200).message("Success"));
                });

        from("direct:notifyGoogleChat")
                .setBody(simple("{\"text\": \"Failure at ${header.targetUrl}\"}"))
                .to("{{google.chat.webhook}}");
    }
}
