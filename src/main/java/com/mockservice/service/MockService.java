package com.mockservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class MockService {

    private static final String CONTROLLER_SUFFIX = "Controller";
    private static final String PATH_DELIMITER = "/";
    private static final String PATH_DELIMITER_SUBSTITUTE = "_";
    private static final String DEFAULT_EXTENSION = ".json";

    private final TemplateService templateService;
    private final ResourceService resourceService;

    public MockService(TemplateService templateService, ResourceService resourceService) {
        this.templateService = templateService;
        this.resourceService = resourceService;
    }

    public String mock(Object controller, HttpServletRequest request) {
        return mock(controller, request, new HashMap<>());
    }

    public String mock(Object controller, HttpServletRequest request, Map<String, String> variables, Map<String, String> params) {
        if (variables != null && params != null)
            variables.putAll(params);
        return mock(controller, request, variables);
    }

    public String mock(Object controller, HttpServletRequest request, Map<String, String> variables) {
        StringBuilder path = new StringBuilder();
        path
                .append(getServiceFolder(controller))
                .append(PATH_DELIMITER)
                .append(request.getMethod().toUpperCase())
                .append(PATH_DELIMITER_SUBSTITUTE)
                .append(encodePath(getPathPattern(request)))
                .append(DEFAULT_EXTENSION);

        return templateService.resolve(resourceService.getAsString(path.toString()), variables);
    }

    private static String getServiceFolder(Object controller) {
        String folder = controller.getClass().getSimpleName();
        int suffixStart = folder.indexOf(CONTROLLER_SUFFIX);
        if (suffixStart < 1)
            throw new IllegalArgumentException("RestController ClassName must end with suffix: " + CONTROLLER_SUFFIX);
        return folder.substring(0, suffixStart);
    }

    private static String getPathPattern(HttpServletRequest request) {
        return (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    }

    private static String encodePath(String path) {
        if (path.startsWith(PATH_DELIMITER))
            path = path.substring(1);
        return String.join(PATH_DELIMITER_SUBSTITUTE, path.split(PATH_DELIMITER));
    }
}
