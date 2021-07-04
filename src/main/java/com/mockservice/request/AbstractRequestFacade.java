package com.mockservice.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractRequestFacade implements RequestFacade {

    private static final Logger log = LoggerFactory.getLogger(AbstractRequestFacade.class);

    private static final String REQUEST_MAPPING_DELIMITER = "/";
    private static final String NAME_DELIMITER = "-";
    private static final String ALT_HEADER = "Mock-Alt";
    private static final String VARIABLE_HEADER = "Mock-Variable";
    private static final String HEADER_SPLIT = "/";

    private final String endpoint;
    private final String encodedEndpoint;
    private final String basePath;
    private final RequestMethod requestMethod;
    private final Map<String, String> pathVariables;
    private final Map<String, String> requestParams = new HashMap<>();
    private final List<String[]> mockVarHeaders;
    private final List<String[]> mockAltHeaders;
    private String body = "";

    @SuppressWarnings("unchecked")
    public AbstractRequestFacade(HttpServletRequest request) {
        endpoint = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        encodedEndpoint = encodeEndpoint(endpoint);
        basePath = getBasePathInternal(request);
        requestMethod = RequestMethod.valueOf(request.getMethod());

        Object o = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (o instanceof Map) {
            pathVariables = (Map<String, String>) o;
        } else {
            pathVariables = new HashMap<>();
        }

        request.getParameterMap().forEach((k, v) -> requestParams.put(k, v[0]));

        mockVarHeaders = getHeadersParts(request, VARIABLE_HEADER);
        mockAltHeaders = getHeadersParts(request, ALT_HEADER);

        try {
            body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            log.warn("Request body processed elsewhere");
        }
    }

    private String encodeEndpoint(String endpoint) {
        if (endpoint.startsWith(REQUEST_MAPPING_DELIMITER)) {
            endpoint = endpoint.substring(1);
        }
        String[] pathParts = endpoint.split(REQUEST_MAPPING_DELIMITER);
        endpoint = String.join(NAME_DELIMITER, pathParts);
        return endpoint.toLowerCase();
    }

    private String getBasePathInternal(HttpServletRequest request) {
        String scheme = getScheme(request);
        String domain = getRemoteHost(request);
        int port = "http".equalsIgnoreCase(scheme) ? 8080 : 8443;
        return scheme + "://" + domain + ":" + port;
    }

    private String getScheme(HttpServletRequest request) {
        String result = request.getHeader("X-Forwarded-Proto");
        if (result == null || result.isEmpty()) {
            result = request.getHeader("X-Forwarded-Protocol");
        }
        if (result == null || result.isEmpty()) {
            result = request.getHeader("X-Url-Scheme");
        }
        if (result == null || result.isEmpty()) {
            result = request.getScheme();
        }
        return result;
    }

    private String getRemoteHost(HttpServletRequest request) {
        String result = request.getHeader("X-Forwarded-For");
        if (result == null || result.isEmpty()) {
            result = request.getRemoteHost();
        }
        return result;
    }

    private List<String[]> getHeadersParts(HttpServletRequest request, String headerName) {
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

    @Override
    public String getBasePath() {
        return basePath;
    }

    @Override
    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public Optional<String> getAlt() {
        for (String[] parts : mockAltHeaders) {
            if (parts.length > 1 && encodedEndpoint.equals(parts[0])) {
                return Optional.of(parts[1]);
            }
        }
        return Optional.empty();
    }

    String getBody() {
        return body;
    }

    Map<String, String> getBaseVariables() {
        Map<String, String> vars = new HashMap<>();
        vars.put("REQUEST_BASE_PATH", getBasePath());
        return vars;
    }

    Map<String, String> getPathVariables() {
        return pathVariables;
    }

    Map<String, String> getRequestParams() {
        return requestParams;
    }

    Map<String, String> getHeaderVariables() {
        Map<String, String> result = new HashMap<>();
        mockVarHeaders.forEach(parts -> {
            if (parts.length > 2 && encodedEndpoint.equals(parts[0])) {
                result.put(parts[1], parts[2]);
            }
        });
        return result;
    }
}
