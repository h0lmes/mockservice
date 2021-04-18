package com.mockservice.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.service.MockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockController {

    @Autowired
    protected MockService mockService;
    private final String folder;

    public MockController() {
        folder = this.getClass().getSimpleName();
    }

    public ResponseEntity<String> mock() {
        return mockService.mock(folder, new HashMap<>());
    }

    public <T> ResponseEntity<T> mock(Class<T> clazz) throws JsonProcessingException {
        return mockService.mock(folder, new HashMap<>(), clazz);
    }

    public <T> ResponseEntity<List<T>> mockList(Class<T> clazz) throws JsonProcessingException {
        return mockService.mockList(folder, new HashMap<>(), clazz);
    }

    public ResponseEntity<String> mock(Map<String, String> variables) {
        return mockService.mock(folder, variables);
    }

    public <T> ResponseEntity<T> mock(Map<String, String> variables, Class<T> clazz) throws JsonProcessingException {
        return mockService.mock(folder, variables, clazz);
    }

    public <T> ResponseEntity<List<T>> mockList(Map<String, String> variables, Class<T> clazz) throws JsonProcessingException {
        return mockService.mockList(folder, variables, clazz);
    }
}
