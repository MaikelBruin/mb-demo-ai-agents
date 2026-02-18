package mb.demo.applications.ai.agents.controllers;

import mb.demo.applications.ai.agents.routes.RouteBuilderConstants;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import mb.demo.applications.ai.agents.webapi.*;

@RestController
public class HasAvailableRestController extends BaseRestController implements AvailableApi {

    public HasAvailableRestController(final ProducerTemplate producerTemplate) {
        super(producerTemplate);
    }

    @Override
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/api/available/rats",
            produces = {"application/json"}
    )
    public ResponseEntity<HasAvailableResponse> getHasAvailableRats() {
        final HasAvailableResponse hasAvailableResponse = producerTemplate.requestBody(RouteBuilderConstants.DIRECT_ROUTE_GET_HAS_AVAILABLE_RATS, null, HasAvailableResponse.class);
        return ResponseEntity.ok(hasAvailableResponse);
    }
}
