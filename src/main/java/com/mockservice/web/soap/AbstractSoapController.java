package com.mockservice.web.soap;

import com.mockservice.service.MockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class AbstractSoapController {

    @Autowired
    @Qualifier("soapMockService")
    MockService mockService;
    private final String folder;

    public AbstractSoapController() {
        folder = this.getClass().getSimpleName();
    }

    public ResponseEntity<String> mock() {
        return mockService.mock(folder, null);
    }

    public ResponseEntity<String> mock(Map<String, String> variables) {
        return mockService.mock(folder, variables);
    }
}
