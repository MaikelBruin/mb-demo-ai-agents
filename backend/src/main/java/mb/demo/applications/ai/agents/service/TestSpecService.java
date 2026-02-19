package mb.demo.applications.ai.agents.service;

import mb.demo.applications.ai.agents.webapi.model.TestResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface TestSpecService {
    List<TestResult> testPublicSpec(MultipartFile file) throws IOException;
}
