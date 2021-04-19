package com.mockservice.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.service.MockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class MockController {

    @Autowired
    protected MockService mockService;
    private final String folder;

    public MockController() {
        folder = this.getClass().getSimpleName();
    }

    public ResponseEntity<String> mock() {
        return mockService.mock(folder, new HashMap<>(), true);
    }

    public <T> ResponseEntity<T> mock(Class<T> clazz) throws JsonProcessingException {
        return mockService.mock(folder, new HashMap<>(), true, clazz);
    }

    public ResponseEntity<String> mock(Map<String, String> variables, boolean useBodyAsVariables) {
        return mockService.mock(folder, variables, useBodyAsVariables);
    }

    public <T> ResponseEntity<T> mock(Map<String, String> variables, boolean useBodyAsVariables, Class<T> clazz) throws JsonProcessingException {
        return mockService.mock(folder, variables, useBodyAsVariables, clazz);
    }
}
