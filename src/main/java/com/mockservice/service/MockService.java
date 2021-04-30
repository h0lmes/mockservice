package com.mockservice.service;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface MockService {
    ResponseEntity<String> mock(String folder, Map<String, String> variables);
}
