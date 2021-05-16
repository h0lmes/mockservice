package com.mockservice.service;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface MockService {
    ResponseEntity<String> mock(String group, Map<String, String> variables);
}
