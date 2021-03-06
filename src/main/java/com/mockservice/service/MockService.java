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
    private static final String MOCK_TIMEOUT_HEADER = "Mock-Timeout";
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

        mockTimeout(controller, request);

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

        mockTimeout(controller, request);

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

        mockTimeout(controller, request);

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

    private static void mockTimeout(Object controller, HttpServletRequest request) {
        String serviceName = getFolder(controller).toLowerCase();
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

    private static String getPath(Object controller, HttpServletRequest request) {
        String folder = getFolder(controller);

        StringBuilder path = new StringBuilder("classpath:");
        return path
                .append(folder)
                .append(PATH_DELIMITER)
                .append(request.getMethod().toUpperCase())
                .append(PATH_DELIMITER_SUBSTITUTE)
                .append(encodePath(getPathPattern(request)))
                .append(getMockOption(folder, request))
                .append(DEFAULT_FILE_EXTENSION)
                .toString();
    }

    private static String getFolder(Object controller) {
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
                    return MOCK_OPTION_DELIMITER + option.substring(serviceName.length() + 1);
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
