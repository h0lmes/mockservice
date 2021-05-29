package com.mockservice.service;

import com.mockservice.domain.Route;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface MockService {
    void cacheRemove(Route route);
    ResponseEntity<String> mock(Map<String, String> variables);
    String mockError(Throwable t);
}
