package mb.demo.applications.ai.agents.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import mb.demo.applications.ai.agents.services.AgentService;
import mb.demo.applications.ai.agents.webapi.api.ReportingApi;
import mb.demo.applications.ai.agents.webapi.model.TestResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReportRestController implements ReportingApi {

    private final AgentService agentService;
    private final ObjectMapper objectMapper;

    public ReportRestController(final AgentService agentService,
                                final ObjectMapper objectMapper) {
        this.agentService = agentService;
        this.objectMapper = objectMapper;
    }

    @Override
    @PostMapping(value = "/api/report/html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> generateHtmlReport(List<TestResult> testResult) {
        String response;
        try {
            response = agentService.getReport(objectMapper.writeValueAsString(testResult));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok(response);
    }
}
