package com.mockservice.web.rest;

import com.mockservice.service.MockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

public class AbstractRestController {

    private static final Logger log = LoggerFactory.getLogger(AbstractRestController.class);

    @Autowired
    @Qualifier("rest")
    MockService mockService;

    public ResponseEntity<String> mock() {
        return mockService.mock(this.getClass().getSimpleName(), null);
    }

    public ResponseEntity<String> mock(Map<String, String> variables) {
        return mockService.mock(this.getClass().getSimpleName(), variables);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Throwable t, WebRequest request) {
        log.error("", t);

        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(t));
    }

    private static class ErrorInfo {

        private String type;
        private String message;

        public ErrorInfo(Throwable t) {
            this(t.getClass().getSimpleName(), t.getLocalizedMessage());
        }

        public ErrorInfo(String type, String message) {
            this.type = type;
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }
    }
}
