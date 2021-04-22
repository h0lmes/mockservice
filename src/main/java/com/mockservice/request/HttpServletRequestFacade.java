package com.mockservice.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpServletRequestFacade {

    private static final String MESSAGE_SERVICE_NAME_MUST_NOT_BE_NULL = "Service name must not be null";
    private static final String PATH_DELIMITER = "/";
    private static final String PATH_DELIMITER_SUBSTITUTE = "_";
    private static final String DEFAULT_FILE_EXTENSION = ".json";
    private static final String MOCK_HEADER = "Mock";
    private static final String MOCK_TIMEOUT_HEADER = "Mock-Timeout";
    private static final String MOCK_VARIABLE_HEADER = "Mock-Variable";
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

        Enumeration<String> headers = request.getHeaders(MOCK_VARIABLE_HEADER);
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            if (header != null && !header.isEmpty()) {
                String[] parts = header.split(PATH_DELIMITER);
                if (parts.length == 3 && serviceName.equals(parts[0])) {
                    result.put(parts[1], parts[2]);
                } else if (parts.length > 3 && serviceName.equals(parts[0]) && endpoint.equals(parts[1])) {
                    result.put(parts[2], parts[3]);
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
        return "classpath:"
                + folder
                + PATH_DELIMITER
                + request.getMethod().toUpperCase()
                + PATH_DELIMITER_SUBSTITUTE
                + getEncodedEndpoint(request)
                + getMockOption(folder, request)
                + DEFAULT_FILE_EXTENSION;
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
            String[] parts = option.split(PATH_DELIMITER);
            if (parts.length == 2 && serviceName.equals(parts[0])) {
                return MOCK_OPTION_DELIMITER + parts[1];
            } else if (parts.length > 2 && serviceName.equals(parts[0]) && endpoint.equals(parts[1])) {
                return MOCK_OPTION_DELIMITER + parts[2];
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
            String[] parts = option.split(PATH_DELIMITER);
            if (parts.length == 2 && serviceName.equals(parts[0])) {
                sleep(parts[1]);
            } else if (parts.length > 2 && serviceName.equals(parts[0]) && endpoint.equals(parts[1])) {
                sleep(parts[2]);
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
        Queue<Pair<String, Pair<String, Object>>> q = new ArrayDeque<>();
        Map<String, String> result = new HashMap<>();

        map.forEach((k, v) -> q.offer(new Pair<>("", new Pair<>(k, v))));

        while (!q.isEmpty()) {
            Pair<String, Pair<String, Object>> el = q.poll();
            String parentKey = el.getKey();
            String key = el.getValue().getKey();
            Object obj = el.getValue().getValue();
            if (obj instanceof Map) {
                map = (Map<String, Object>) obj;
                map.forEach((k, v) -> q.offer(new Pair<>(parentKey.isEmpty() ? key : parentKey + "." + key, new Pair<>(k, v))));
            } else {
                result.put(parentKey.isEmpty() ? key : parentKey + "." + key, obj.toString());
            }
        }
        return result;
    }

    public static void main(String[] args) throws JsonProcessingException {
        String json =
                "{" +
                    "\"key1\": \"value 1\", " +
                    "\"key2\": {" +
                        "\"key1\": \"2021-04-19\"," +
                        "\"key2\": {" +
                            "\"key1\": 10101, " +
                            "\"key2\": [" +
                                "\"value 1\", \"value 2\"" +
                            "]" +
                        "}" +
                    "}" +
                "}";
        jsonToMap(json).forEach((k, v) -> System.out.println(k + " -> " + v));
    }
}
