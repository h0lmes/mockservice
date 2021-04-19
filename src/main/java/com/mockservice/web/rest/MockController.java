package com.mockservice.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.service.MockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class MockController {

    @Autowired
    protected MockService mockService;
    private final String folder;

    public MockController() {
        folder = this.getClass().getSimpleName();
    }

    public ResponseEntity<String> mock() {
        return mockService.mock(folder);
    }

    public <T> ResponseEntity<T> mock(Class<T> clazz) throws JsonProcessingException {
        return mockService.mock(folder, clazz);
    }

    public <T> ResponseEntity<List<T>> mockList(Class<T> clazz) throws JsonProcessingException {
        return mockService.mockList(folder, clazz);
    }
}
