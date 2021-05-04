package com.mockservice.web.rest.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e, WebRequest request) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }
}
