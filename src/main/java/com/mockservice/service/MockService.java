package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.request.HttpServletRequestFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MockService {

    @Autowired
    private HttpServletRequest request;
    private final ResourceService resourceService;
    private final TemplateService templateService;

    public MockService(ResourceService resourceService, TemplateService templateService) {
        this.resourceService = resourceService;
        this.templateService = templateService;
    }

    public ResponseEntity<String> mock(String folder, Map<String, String> variables) {
        HttpServletRequestFacade requestFacade = new HttpServletRequestFacade(request, folder);
        ResourceWrapper resource = resourceService.getAsWrapper(requestFacade.getPath());

        requestFacade.mockTimeout();

        return ResponseEntity
                .status(resource.getCode())
                .headers(resource.getHeaders())
                .body(templateService.resolve(resource.getBody(), requestFacade.getVariables(variables)));
    }

    public <T> ResponseEntity<T> mock(String folder, Map<String, String> variables, Class<T> clazz) throws JsonProcessingException {
        HttpServletRequestFacade requestFacade = new HttpServletRequestFacade(request, folder);
        ResourceWrapper resource = resourceService.getAsWrapper(requestFacade.getPath());

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructType(clazz);
        T body = mapper.readValue(templateService.resolve(resource.getBody(), requestFacade.getVariables(variables)), type);

        requestFacade.mockTimeout();

        return ResponseEntity
                .status(resource.getCode())
                .headers(resource.getHeaders())
                .body(body);
    }

    public <T> ResponseEntity<List<T>> mockList(String folder, Map<String, String> variables, Class<T> clazz) throws JsonProcessingException {
        HttpServletRequestFacade requestFacade = new HttpServletRequestFacade(request, folder);
        ResourceWrapper resource = resourceService.getAsWrapper(requestFacade.getPath());

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
        List<T> body = mapper.readValue(templateService.resolve(resource.getBody(), requestFacade.getVariables(variables)), type);

        requestFacade.mockTimeout();

        return ResponseEntity
                .status(resource.getCode())
                .headers(resource.getHeaders())
                .body(body);
    }
}
