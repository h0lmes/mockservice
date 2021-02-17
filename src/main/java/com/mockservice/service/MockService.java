package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MockService {

    private static final String CONTROLLER_SUFFIX = "Controller";
    private static final String PATH_DELIMITER = "/";
    private static final String PATH_DELIMITER_SUBSTITUTE = "_";
    private static final String DEFAULT_FILE_EXTENSION = ".json";

    private final ResourceService resourceService;
    private final TemplateService templateService;

    public MockService(ResourceService resourceService, TemplateService templateService) {
        this.resourceService = resourceService;
        this.templateService = templateService;
    }

    /**
     * General mock method to call
     */
    public String mock(Object controller, HttpServletRequest request, Map<String, String> variables) {
        String path = getPath(controller, request);
        String resourceString = resourceService.getAsString(path);
        return templateService.resolve(resourceString, variables);
    }

    public <T> T mockTyped(Object controller, HttpServletRequest request, Map<String, String> variables, Class<T> clazz) throws JsonProcessingException {
        String result = mock(controller, request, variables);
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructType(clazz);
        return mapper.readValue(result, type);
    }

    public <T> List<T> mockTypedList(Object controller, HttpServletRequest request, Map<String, String> variables, Class<T> clazz) throws JsonProcessingException {
        String result = mock(controller, request, variables);
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
        return mapper.readValue(result, type);
    }

    /**
     * Method to call with no variables map
     */
    public String mock(Object controller, HttpServletRequest request) {
        return mock(controller, request, new HashMap<>());
    }

    public <T> T mockTyped(Object controller, HttpServletRequest request, Class<T> clazz) throws JsonProcessingException {
        return mockTyped(controller, request, new HashMap<>(), clazz);
    }

    public <T> List<T> mockTypedList(Object controller, HttpServletRequest request, Class<T> clazz) throws JsonProcessingException {
        return mockTypedList(controller, request, new HashMap<>(), clazz);
    }

    //--------------------------------------------------------------------------
    //
    //
    //
    //
    //
    // static and private methods here
    //
    //
    //
    //
    //
    //--------------------------------------------------------------------------

    private String getPath(Object controller, HttpServletRequest request) {
        StringBuilder path = new StringBuilder();
        path
                .append(getServiceFolder(controller))
                .append(PATH_DELIMITER)
                .append(request.getMethod().toUpperCase())
                .append(PATH_DELIMITER_SUBSTITUTE)
                .append(encodePath(getPathPattern(request)))
                .append(DEFAULT_FILE_EXTENSION);
        return path.toString();
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
