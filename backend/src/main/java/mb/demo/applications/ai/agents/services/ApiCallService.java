package mb.demo.applications.ai.agents.services;

import jakarta.annotation.Nullable;
import mb.demo.applications.ai.agents.models.ApiCall;
import mb.demo.applications.ai.agents.models.ApiCallResponse;

public interface ApiCallService {
    ApiCallResponse executeCall(ApiCall call, @Nullable String token);
}
