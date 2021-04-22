package com.mockservice.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class MockExceptionHandler {

    @ExceptionHandler
    protected ResponseEntity<Object> handleException(Exception e, WebRequest request) {
        return ResponseEntity.badRequest().body(getErrorBody(e));
    }

    private static class ErrorBody {
        private String type;
        private String message;
        public ErrorBody(String type, String message) {
            this.type = type;
            this.message = message;
        }
        public String getType() { return type; }
        public String getMessage() { return message; }
    }

    private Object getErrorBody(Exception e) {
        return new ErrorBody(e.getClass().getSimpleName(), e.getMessage());
    }
}
