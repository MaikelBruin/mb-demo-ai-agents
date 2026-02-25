package mb.demo.applications.ai.agents.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class FileUtils {

    /**
     * Loads a resource from the classpath (e.g., src/main/resources) as a File object.
     *
     * @param resourceName The name of the file in the resources folder (e.g., "specs/petstore.yaml").
     * @return A File object pointing to the resource.
     * @throws IllegalArgumentException if the resource is not found.
     * @throws URISyntaxException if the resource URL is malformed.
     */
    public static File getFileFromResources(String resourceName) throws URISyntaxException {
        // Use the context class loader to get the resource URL
        URL resourceUrl = FileUtils.class.getClassLoader().getResource(resourceName);

        // Validate that the resource was found
        Objects.requireNonNull(resourceUrl, "Resource not found: " + resourceName);

        // Convert the URL to a File object
        return new File(resourceUrl.toURI());
    }

    public static void writeToFile(String path, String data) throws IOException {
        Path filePath = Paths.get(path);
        Files.write(filePath, data.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}