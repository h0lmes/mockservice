package com.mockservice.web.ws;

import com.mockservice.service.MockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class AbstractWsController {

    @Autowired
    @Qualifier("wsMockService")
    MockService mockService;
    private final String folder;

    public AbstractWsController() {
        folder = this.getClass().getSimpleName();
    }

    public ResponseEntity<String> mock() {
        return mockService.mock(folder, null);
    }

    public ResponseEntity<String> mock(Map<String, String> variables) {
        return mockService.mock(folder, variables);
    }
}
