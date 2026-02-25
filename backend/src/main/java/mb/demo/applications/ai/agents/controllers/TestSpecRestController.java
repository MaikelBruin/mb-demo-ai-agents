package mb.demo.applications.ai.agents.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import mb.demo.applications.ai.agents.services.TestSpecService;
import mb.demo.applications.ai.agents.webapi.api.DefaultApi;
import mb.demo.applications.ai.agents.webapi.model.TestResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class TestSpecRestController implements DefaultApi {

    private final TestSpecService testSpecService;

    public TestSpecRestController(final TestSpecService testSpecService) {
        this.testSpecService = testSpecService;
    }

    @Override
    @PostMapping("/api/test/public/openapi")
    public ResponseEntity<List<TestResult>> testPublicSpec(
            @Parameter(name = "file", required = true) @RequestPart(value = "file") MultipartFile file
    ) {
        List<TestResult> response;
        try {
            response = testSpecService.testPublicSpec(file, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(response);
    }
}
