package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
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
    private static final String MOCK_HEADER = "Mock";
    private static final String MOCK_HEADER_SPLIT_REGEX = "\\s+";
    private static final String MOCK_OPTION_DELIMITER = "#";

    private final ResourceService resourceService;
    private final TemplateService templateService;

    public MockService(ResourceService resourceService, TemplateService templateService) {
        this.resourceService = resourceService;
        this.templateService = templateService;
    }

    /**
     * General mock method to call
     */
    public ResponseEntity<String> mock(Object controller, HttpServletRequest request, Map<String, String> variables) {
        ResourceWrapper resource = resourceService.getAsWrapper(getPath(controller, request));

        return ResponseEntity
                .status(resource.getCode())
                .headers(resource.getHeaders())
                .body(templateService.resolve(resource.getBody(), variables));
    }

    public <T> ResponseEntity<T> mockTyped(Object controller, HttpServletRequest request, Map<String, String> variables, Class<T> clazz) throws JsonProcessingException {
        ResourceWrapper resource = resourceService.getAsWrapper(getPath(controller, request));

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructType(clazz);
        T body = mapper.readValue(templateService.resolve(resource.getBody(), variables), type);

        return ResponseEntity
                .status(resource.getCode())
                .headers(resource.getHeaders())
                .body(body);
    }

    public <T> ResponseEntity<List<T>> mockTypedList(Object controller, HttpServletRequest request, Map<String, String> variables, Class<T> clazz) throws JsonProcessingException {
        ResourceWrapper resource = resourceService.getAsWrapper(getPath(controller, request));

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
        List<T> body = mapper.readValue(templateService.resolve(resource.getBody(), variables), type);

        return ResponseEntity
                .status(resource.getCode())
                .headers(resource.getHeaders())
                .body(body);
    }

    /**
     * Method to call with no variables map
     */
    public ResponseEntity<String> mock(Object controller, HttpServletRequest request) {
        return mock(controller, request, new HashMap<>());
    }

    public <T> ResponseEntity<T> mockTyped(Object controller, HttpServletRequest request, Class<T> clazz) throws JsonProcessingException {
        return mockTyped(controller, request, new HashMap<>(), clazz);
    }

    public <T> ResponseEntity<List<T>> mockTypedList(Object controller, HttpServletRequest request, Class<T> clazz) throws JsonProcessingException {
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

    private static String getPath(Object controller, HttpServletRequest request) {
        String serviceFolder = getServiceFolder(controller);
        String mockOption = getMockOption(serviceFolder, request);

        StringBuilder path = new StringBuilder("classpath:");
        return path
                .append(serviceFolder)
                .append(PATH_DELIMITER)
                .append(request.getMethod().toUpperCase())
                .append(PATH_DELIMITER_SUBSTITUTE)
                .append(encodePath(getPathPattern(request)))
                .append(mockOption.isEmpty() ? "" : MOCK_OPTION_DELIMITER + mockOption)
                .append(DEFAULT_FILE_EXTENSION)
                .toString();
    }

    private static String getServiceFolder(Object controller) {
        String folder = controller.getClass().getSimpleName();
        int suffixStart = folder.indexOf(CONTROLLER_SUFFIX);
        if (suffixStart < 1)
            throw new IllegalArgumentException("RestController ClassName must end with suffix: " + CONTROLLER_SUFFIX);
        return folder.substring(0, suffixStart);
    }

    private static String getMockOption(String serviceName, HttpServletRequest request) {
        serviceName = serviceName.toLowerCase();
        String header = request.getHeader(MOCK_HEADER);
        if (header != null) {
            header = header.trim().toLowerCase();
            for (String option : header.split(MOCK_HEADER_SPLIT_REGEX)) {
                if (option.startsWith(serviceName)) {
                    return option.substring(serviceName.length() + 1);
                }
            }
        }
        return "";
    }

    private static String encodePath(String path) {
        if (path.startsWith(PATH_DELIMITER))
            path = path.substring(1);
        return String.join(PATH_DELIMITER_SUBSTITUTE, path.split(PATH_DELIMITER));
    }

    private static String getPathPattern(HttpServletRequest request) {
        return (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    }
}