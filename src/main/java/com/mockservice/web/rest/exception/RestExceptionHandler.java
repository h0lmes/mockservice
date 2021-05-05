package com.mockservice.web.rest.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e, WebRequest request) {
        log.error("", e);

        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }
}
