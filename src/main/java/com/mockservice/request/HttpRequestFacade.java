package com.mockservice.request;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class HttpRequestFacade {

    static final String PATH_DELIMITER = "/";
    static final String PATH_DELIMITER_SUBSTITUTE = "_";
    private static final String MOCK_HEADER = "Mock";
    private static final String MOCK_TIMEOUT_HEADER = "Mock-Timeout";
    private static final String MOCK_VARIABLE_HEADER = "Mock-Variable";
    private static final String MOCK_HEADER_SPLIT_REGEX = "\\s+";
    private static final String MOCK_OPTION_DELIMITER = "#";

    private String folder;
    private HttpServletRequest request;

    public HttpRequestFacade(@NonNull HttpServletRequest request,
                             @NonNull String folder) {
        Assert.notNull(request, "Request must not be null");
        Assert.notNull(folder, "Folder must not be null");
        this.request = request;
        this.folder = folder;
    }

    public String getFolder() {
        return folder;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    String getBody() {
        try {
            return request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            // Body processed elsewhere. Do nothing.
            return null;
        }
    }

    public abstract String getPath();

    public abstract Map<String, String> getVariables(@Nullable Map<String, String> variables);

    @SuppressWarnings("unchecked")
    Map<String, String> getPathVariables() {
        Object pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables instanceof Map) {
            return (Map<String, String>) pathVariables;
        }
        return new HashMap<>();
    }

    Map<String, String> getRequestParams() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            result.put(entry.getKey(), entry.getValue()[0]);
        }
        return result;
    }

    Map<String, String> getMockVariables() {
        Map<String, String> result = new HashMap<>();
        String endpoint = getEncodedEndpoint();

        Enumeration<String> headers = request.getHeaders(MOCK_VARIABLE_HEADER);
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            if (header != null && !header.isEmpty()) {
                String[] parts = header.trim().split(PATH_DELIMITER);
                if (parts.length == 3 && folder.equalsIgnoreCase(parts[0])) {
                    result.put(parts[1], parts[2]);
                } else if (parts.length > 3 && folder.equalsIgnoreCase(parts[0]) && endpoint.equals(parts[1])) {
                    result.put(parts[2], parts[3]);
                }
            }
        }
        return result;
    }

    String getEncodedEndpoint() {
        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (path.startsWith(PATH_DELIMITER)) {
            path = path.substring(1);
        }
        return String.join(PATH_DELIMITER_SUBSTITUTE, path.split(PATH_DELIMITER)).toLowerCase();
    }

    String getMockOption() {
        String endpoint = getEncodedEndpoint();

        Enumeration<String> headers = request.getHeaders(MOCK_HEADER);
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            if (header != null && !header.isEmpty()) {

                for (String option : header.split(MOCK_HEADER_SPLIT_REGEX)) {
                    String[] parts = option.split(PATH_DELIMITER);
                    if (parts.length == 2 && folder.equalsIgnoreCase(parts[0])) {
                        return MOCK_OPTION_DELIMITER + parts[1];
                    } else if (parts.length > 2 && folder.equalsIgnoreCase(parts[0]) && endpoint.equals(parts[1])) {
                        return MOCK_OPTION_DELIMITER + parts[2];
                    }
                }
            }
        }
        return "";
    }

    public void mockTimeout() {
        String endpoint = getEncodedEndpoint();

        Enumeration<String> headers = request.getHeaders(MOCK_TIMEOUT_HEADER);
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            if (header != null && !header.isEmpty()) {

                for (String option : header.split(MOCK_HEADER_SPLIT_REGEX)) {
                    String[] parts = option.split(PATH_DELIMITER);
                    if (parts.length == 2 && folder.equalsIgnoreCase(parts[0])) {
                        sleep(parts[1]);
                    } else if (parts.length > 2 && folder.equalsIgnoreCase(parts[0]) && endpoint.equals(parts[1])) {
                        sleep(parts[2]);
                    }
                }
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
}
