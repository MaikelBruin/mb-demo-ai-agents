package mb.demo.applications.ai.agents.utils;

import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
public class OpenApiUtils {
    private static String generateStringExample(String format) {
        if (format == null) return "example_string";

        return switch (format.toLowerCase()) {
            // Official OpenAPI Formats
            case "date" -> LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            case "date-time" -> OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            case "password" -> "s3cureP@ssw0rd!";
            case "byte" -> Base64.getEncoder().encodeToString("Hello World".getBytes());
            case "binary" -> "01010111_binary_data"; // Usually handled as file stream in real calls

            // Common Community Formats
            case "uuid" -> UUID.randomUUID().toString();
            case "email" -> "test-user@example.com";
            case "uri", "url" -> "https://api.example.com/v1/resource";
            case "hostname" -> "api.example.com";
            case "ipv4" -> "192.168.1.1";
            case "ipv6" -> "2001:0db8:85a3:0000:0000:8a2e:0370:7334";

            default -> "example_" + format;
        };
    }

    @NotNull
    public static Object getExampleValue(Parameter parameter) {
        Object exampleValue = parameter.getExample() != null
                ? parameter.getExample()
                : parameter.getSchema().getDefault();
        //check for array
        if (parameter.getSchema().getItems() != null && parameter.getSchema().getItems().getType() != null) {
            Object itemExample = parameter.getSchema().getItems().getExample();
            if (itemExample == null) {
                String type = parameter.getSchema().getItems().getType();
                if (type.equalsIgnoreCase("string")) exampleValue = "[someString]";
                else exampleValue = "[123]";
            }
        }

        //set default value if no examples are listed in spec
        if (exampleValue == null) {
            exampleValue = switch (parameter.getSchema().getType().toUpperCase()) {
                case "STRING" -> OpenApiUtils.generateStringExample(parameter.getSchema().getFormat());
                case "NUMBER", "INTEGER" -> 1;
                case "BOOLEAN" -> true;
                case "OBJECT" -> new Object();
                default -> exampleValue;
            };
        }

        if (exampleValue == null)
            throw new RuntimeException("Could not find example value for parameter: " + parameter.getName());
        return exampleValue;
    }

    public static String replacePathParameters(List<Parameter> parameters, String url) {
        if (parameters != null) {
            List<Parameter> pathParams = parameters
                    .stream()
                    .filter(parameter -> parameter.getIn().equalsIgnoreCase("path"))
                    .toList();
            if (!pathParams.isEmpty()) {
                for (Parameter parameter : pathParams) {
                    String name = parameter.getName();
                    Object exampleValue = getExampleValue(parameter);
                    String paramInPath = "{" + name + "}";
                    url = url.replace(paramInPath, exampleValue.toString());
                }

                log.info("fixed url for path params: '{}'", url);
            }
        }
        return url;
    }

    public static String addRequiredQueryParameters(List<Parameter> parameters, String url) {
        if (parameters != null) {
            List<Parameter> requiredQueryParams = parameters
                    .stream()
                    .filter(parameter -> parameter.getIn().equalsIgnoreCase("query")
                            && parameter.getRequired())
                    .toList();
            if (!requiredQueryParams.isEmpty()) {
                url = url.concat("?");
                for (Parameter parameter : requiredQueryParams) {
                    String name = parameter.getName();
                    Object exampleValue = getExampleValue(parameter);
                    url = url.concat(name + "=" + exampleValue + "&");
                }
                url = url.substring(0, url.length() - 1);
                log.info("added required query params: '{}'", url);
            }
        }
        return url;
    }
}
