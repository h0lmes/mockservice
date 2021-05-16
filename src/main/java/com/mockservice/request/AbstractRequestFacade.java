package com.mockservice.request;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractRequestFacade implements RequestFacade {

    private static final String REQUEST_MAPPING_DELIMITER = "/";
    static final String NAME_DELIMITER = "-";
    private static final String SUFFIX_HEADER = "Mock-Suffix";
    private static final String SUFFIX_DELIMITER = "--";
    private static final String VARIABLE_HEADER = "Mock-Variable";
    private static final String HEADER_SPLIT = "/";

    private String group;
    private HttpServletRequest request;
    private String endpoint;

    public AbstractRequestFacade(@NonNull String group, @NonNull HttpServletRequest request) {
        this.group = group;
        this.request = request;
        this.endpoint = encodeEndpoint(request);
    }

    private String encodeEndpoint(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (path.startsWith(REQUEST_MAPPING_DELIMITER)) {
            path = path.substring(1);
        }
        String[] pathParts = path.split(REQUEST_MAPPING_DELIMITER);
        path = String.join(NAME_DELIMITER, pathParts);
        return path.toLowerCase();
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getMethod() {
        return getRequest().getMethod().toLowerCase();
    }

    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    String getEndpoint() {
        return endpoint;
    }

    Stream<String> getBody() {
        try {
            return request.getReader().lines();
        } catch (Exception e) {
            // Body processed elsewhere. Do nothing.
            return Stream.empty();
        }
    }

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
        request.getParameterMap().forEach(
                (k, v) -> result.put(k, v[0])
        );
        return result;
    }

    private List<String[]> getHeadersParts(String headerName) {
        List<String[]> result = new ArrayList<>();
        Enumeration<String> headers = request.getHeaders(headerName);
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            if (header != null && !header.isEmpty()) {
                result.add(header.trim().split(HEADER_SPLIT));
            }
        }
        return result;
    }

    Map<String, String> getHeaderVariables() {
        Map<String, String> result = new HashMap<>();
        getHeadersParts(VARIABLE_HEADER).forEach(parts -> {
            if (parts.length == 3 && group.equalsIgnoreCase(parts[0])) {
                result.put(parts[1], parts[2]);
            } else if (parts.length > 3 && group.equalsIgnoreCase(parts[0]) && endpoint.equals(parts[1])) {
                result.put(parts[2], parts[3]);
            }
        });
        return result;
    }

    String getSuffix() {
        for (String[] parts : getHeadersParts(SUFFIX_HEADER)) {
            if (parts.length == 2 && group.equalsIgnoreCase(parts[0])) {
                return SUFFIX_DELIMITER + parts[1];
            } else if (parts.length > 2 && group.equalsIgnoreCase(parts[0]) && endpoint.equals(parts[1])) {
                return SUFFIX_DELIMITER + parts[2];
            }
        }
        return "";
    }
}
