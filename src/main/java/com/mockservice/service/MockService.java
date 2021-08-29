package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.request.RequestFacade;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MockService {
    void cacheRemove(Route route);
    ResponseEntity<String> mock(RequestFacade request);
    Map<String, String> getGlobalVariables();
    Map<String, String> putGlobalVariables(Optional<Map<String, String>> variables);
    Map<String, String> removeGlobalVariables(Optional<List<String>> keys);
}
