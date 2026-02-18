package mb.demo.applications.ai.agents.service.impl;

import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.ai.agents.service.HasAvailableService;
import mb.demo.applications.ai.agents.webapi.model.HasAvailableResponse;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HasAvailableServiceImpl implements HasAvailableService {

    @Override
    public HasAvailableResponse getHasAvailableRats() {
        HasAvailableResponse hasAvailableResponse = new HasAvailableResponse();
        hasAvailableResponse.setHasAvailable(true);
        return hasAvailableResponse;

    }
}
