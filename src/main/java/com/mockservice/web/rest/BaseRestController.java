package com.mockservice.web.rest;

import com.mockservice.config.RegistrableController;
import com.mockservice.mockconfig.RouteType;
import com.mockservice.service.MockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

public class BaseRestController implements RegistrableController {

    private static final Logger log = LoggerFactory.getLogger(BaseRestController.class);

    @Autowired
    @Qualifier("rest")
    MockService mockService;

    public ResponseEntity<String> mock() {
        return mockService.mock(null);
    }

    public ResponseEntity<String> mock(Map<String, String> variables) {
        return mockService.mock(variables);
    }

    @Override
    public RouteType getRouteType() {
        return RouteType.REST;
    }

    @Override
    public String getRouteGroup() {
        return getClass().getSimpleName();
    }

    @ExceptionHandler
    protected ResponseEntity<String> handleException(Throwable t) {
        log.error("", t);
        return ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mockService.mockError(t));
    }
}
