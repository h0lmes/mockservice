package com.mockservice.service;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface MockService {
    ResponseEntity<String> mock(Map<String, String> variables);
    String mockError(Throwable t);
}
