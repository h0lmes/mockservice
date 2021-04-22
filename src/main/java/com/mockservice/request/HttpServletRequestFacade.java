package com.mockservice.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpServletRequestFacade {

    private static final String MESSAGE_SERVICE_NAME_MUST_NOT_BE_NULL = "Service name must not be null";
    private static final String PATH_DELIMITER = "/";
    private static final String PATH_DELIMITER_SUBSTITUTE = "_";
    private static final String DEFAULT_FILE_EXTENSION = ".json";
    private static final String MOCK_HEADER = "Mock";
    private static final String MOCK_TIMEOUT_HEADER = "Mock-Timeout";
    private static final String MOCK_VARIABLES_HEADER = "Mock-Variables";
    private static final String MOCK_HEADER_SPLIT_REGEX = "\\s+";
    private static final String MOCK_OPTION_DELIMITER = "#";

    private HttpServletRequest request;
    private String folder;

    public HttpServletRequestFacade(HttpServletRequest request, String folder) {
        this.request = request;
        this.folder = folder;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getVariables(@NonNull Map<String, String> variables, boolean useBodyAsVariables) {
        Assert.notNull(variables, "Variables must not be null");

        if (useBodyAsVariables && !"GET".equalsIgnoreCase(request.getMethod())) {
            try {
                String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                if (body != null && !body.trim().isEmpty()) {
                    Map<String, String> bodyVariables = jsonToMap(body);
                    bodyVariables.forEach(variables::putIfAbsent);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        Object pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables instanceof Map) {
            ((Map<String, String>) pathVariables).forEach(variables::putIfAbsent);
        }

        Map<String, String[]> requestParams = request.getParameterMap();
        requestParams.forEach((k, v) -> variables.putIfAbsent(k, v[0]));

        getMockVariables(folder, request).forEach(variables::put);

        return variables;
    }

    private static Map<String, String> getMockVariables(@NonNull String serviceName, @NonNull HttpServletRequest request) {
        Assert.notNull(serviceName, MESSAGE_SERVICE_NAME_MUST_NOT_BE_NULL);
        Map<String, String> result = new HashMap<>();
        serviceName = serviceName.toLowerCase();
        String endpoint = getEncodedEndpoint(request);
        Enumeration<String> headers = request.getHeaders(MOCK_VARIABLES_HEADER);
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            for (String option : header.trim().toLowerCase().split(MOCK_HEADER_SPLIT_REGEX)) {
                String[] optionParts = option.split(PATH_DELIMITER);

                if (optionParts.length == 3 && serviceName.equals(optionParts[0])) {
                    result.put(optionParts[1], optionParts[2]);
                }

                if (optionParts.length == 4 && serviceName.equals(optionParts[0]) && endpoint.equals(optionParts[1])) {
                    result.put(optionParts[2], optionParts[3]);
                }
            }
        }
        return result;
    }

    public String getPath() {
        return getPath(folder, request);
    }

    private static String getPath(@NonNull String folder, @NonNull HttpServletRequest request) {
        Assert.notNull(folder, "Folder must not be null");
        return "classpath:" +
                folder +
                PATH_DELIMITER +
                request.getMethod().toUpperCase() +
                PATH_DELIMITER_SUBSTITUTE +
                getEncodedEndpoint(request) +
                getMockOption(folder, request) +
                DEFAULT_FILE_EXTENSION;
    }

    private static String getEncodedEndpoint(@NonNull HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (path.startsWith(PATH_DELIMITER)) {
            path = path.substring(1);
        }
        return String.join(PATH_DELIMITER_SUBSTITUTE, path.split(PATH_DELIMITER)).toLowerCase();
    }

    private static String getMockOption(@NonNull String serviceName, @NonNull HttpServletRequest request) {
        Assert.notNull(serviceName, MESSAGE_SERVICE_NAME_MUST_NOT_BE_NULL);
        String header = request.getHeader(MOCK_HEADER);
        if (header == null) {
            return "";
        }

        serviceName = serviceName.toLowerCase();
        String endpoint = getEncodedEndpoint(request);
        for (String option : header.trim().toLowerCase().split(MOCK_HEADER_SPLIT_REGEX)) {
            String[] optionParts = option.split(PATH_DELIMITER);

            if (optionParts.length == 2 && serviceName.equals(optionParts[0])) {
                return MOCK_OPTION_DELIMITER + optionParts[1];
            }

            if (optionParts.length == 3 && serviceName.equals(optionParts[0]) && endpoint.equals(optionParts[1])) {
                return MOCK_OPTION_DELIMITER + optionParts[2];
            }
        }

        return "";
    }

    public void mockTimeout() {
        mockTimeout(folder, request);
    }

    private static void mockTimeout(@NonNull String serviceName, @NonNull HttpServletRequest request) {
        Assert.notNull(serviceName, MESSAGE_SERVICE_NAME_MUST_NOT_BE_NULL);
        String header = request.getHeader(MOCK_TIMEOUT_HEADER);
        if (header == null) {
            return;
        }

        serviceName = serviceName.toLowerCase();
        String endpoint = getEncodedEndpoint(request);
        for (String option : header.trim().toLowerCase().split(MOCK_HEADER_SPLIT_REGEX)) {
            String[] optionParts = option.split(PATH_DELIMITER);

            if (optionParts.length == 2 && serviceName.equals(optionParts[0])) {
                sleep(optionParts[1]);
            }

            if (optionParts.length == 3 && serviceName.equals(optionParts[0]) && endpoint.equals(optionParts[1])) {
                sleep(optionParts[2]);
            }
        }
    }

    private static void sleep(String ms) {
        try {
            Thread.sleep(Long.valueOf(ms));
        } catch (NumberFormatException e) {
            // do nothing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> jsonToMap(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        Map<String, Object> map = mapper.readValue(json, Map.class);
        return flattenMap(map);
    }

    private static Map<String, String> flattenMap(Map<String, Object> map) {
        // TODO
        return flattenMapRec(map);
    }

    private static Map<String, String> flattenMapRec(Map<String, Object> map) {
        return map.entrySet().stream()
                .flatMap(e -> flatten(e, e.getKey() + "."))
                .collect(Collectors.toMap( Map.Entry::getKey, e -> String.valueOf(e.getValue()) ));
    }

    @SuppressWarnings("unchecked")
    private static Stream<Map.Entry<String, ?>> flatten(Map.Entry<String, ?> entry, String keyPrefix) {
        if (entry.getValue() instanceof Map) {
            return ((Map<String,?>) entry.getValue()).entrySet().stream().flatMap(e -> flatten(e, keyPrefix + e.getKey() + "."));
        }
        return Stream.of(entry);
    }

    public static void main(String[] args) throws JsonProcessingException {
        String json =
                "{" +
                    "\"key1\": \"value 1\", " +
                    "\"key2\": {" +
                        "\"key2.1\": \"2021-04-19\"," +
                        "\"key2.2\": {" +
                            "\"key2.2.1\": 10101, " +
                            "\"key2.2.2\": [" +
                                "\"value 1\", \"value 2\"" +
                            "]" +
                        "}" +
                    "}" +
                "}";
        jsonToMap(json).forEach((k, v) -> System.out.println(k + " : " + v));
    }
}
