package com.mockservice.request;

import org.springframework.web.servlet.HandlerMapping;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class HttpServletRequestFacade {

    private static final String PATH_DELIMITER = "/";
    private static final String PATH_DELIMITER_SUBSTITUTE = "_";
    private static final String DEFAULT_FILE_EXTENSION = ".json";
    private static final String MOCK_HEADER = "Mock";
    private static final String MOCK_TIMEOUT_HEADER = "Mock-Timeout";
    private static final String MOCK_HEADER_SPLIT_REGEX = "\\s+";
    private static final String MOCK_OPTION_DELIMITER = "#";

    private HttpServletRequest request;
    private String folder;

    public HttpServletRequestFacade(HttpServletRequest request, String folder) {
        this.request = request;
        this.folder = folder;
    }

    public Map<String, String> getVariables(Map<String, String> appendToVariables) {
        return getVariables(request, appendToVariables);
    }

    public String getPath() {
        return this.getPath(folder, request);
    }

    public void mockTimeout() {
        this.mockTimeout(folder, request);
    }

    private static Map<String, String> getVariables(HttpServletRequest request, Map<String, String> appendToVariables) {
        Object uriVars = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (uriVars instanceof Map) {
            ((Map<String, String>) uriVars).forEach(appendToVariables::putIfAbsent);
        }
        Map<String, String[]> parameterMap = request.getParameterMap();
        parameterMap.forEach((k, v) -> appendToVariables.putIfAbsent(k, v[0]));

        return appendToVariables;
    }

    private static String getPath(String folder, HttpServletRequest request) {
        StringBuilder path = new StringBuilder("classpath:");
        return path
                .append(folder)
                .append(PATH_DELIMITER)
                .append(request.getMethod().toUpperCase())
                .append(PATH_DELIMITER_SUBSTITUTE)
                .append(getEncodedEndpoint(request))
                .append(getMockOption(folder, request))
                .append(DEFAULT_FILE_EXTENSION)
                .toString();
    }

    private static String getEncodedEndpoint(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (path.startsWith(PATH_DELIMITER)) {
            path = path.substring(1);
        }
        return String.join(PATH_DELIMITER_SUBSTITUTE, path.split(PATH_DELIMITER));
    }

    private static String getMockOption(String serviceName, HttpServletRequest request) {
        serviceName = serviceName.toLowerCase();
        String endpoint = getEncodedEndpoint(request);
        String header = request.getHeader(MOCK_HEADER);
        if (header != null) {
            header = header.trim().toLowerCase();
            for (String option : header.split(MOCK_HEADER_SPLIT_REGEX)) {
                String[] optionParts = option.split(PATH_DELIMITER);

                if (optionParts.length == 2) {
                    String serviceNamePart = optionParts[0];
                    String optionNamePart = optionParts[1];
                    if (serviceNamePart.equals(serviceName)) {
                        return MOCK_OPTION_DELIMITER + optionNamePart;
                    }
                }

                if (optionParts.length == 3) {
                    String serviceNamePart = optionParts[0];
                    String endpointPart = optionParts[1];
                    String optionNamePart = optionParts[2];
                    if (serviceNamePart.equals(serviceName) && endpointPart.equals(endpoint)) {
                        return MOCK_OPTION_DELIMITER + optionNamePart;
                    }
                }
            }
        }
        return "";
    }

    private static void mockTimeout(String serviceName, HttpServletRequest request) {
        serviceName = serviceName.toLowerCase();
        String header = request.getHeader(MOCK_TIMEOUT_HEADER);
        if (header != null) {
            for (String option : header.trim().toLowerCase().split(MOCK_HEADER_SPLIT_REGEX)) {
                if (option.startsWith(serviceName)) {
                    long ms = Long.valueOf(option.substring(serviceName.length() + 1));
                    try {
                        Thread.sleep(ms);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
