package mb.demo.applications.ai.agents.base.cucumber;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mb.demo.applications.ai.agents.webapi.model.HasAvailableResponse;

@NoArgsConstructor
@Getter
@Setter
public class TestDataHolder {

    private HasAvailableResponse hasAvailableResponse;
    private Exception exception;
}
