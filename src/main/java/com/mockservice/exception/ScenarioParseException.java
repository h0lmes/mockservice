package com.mockservice.exception;

public class ScenarioParseException extends RuntimeException {

    public ScenarioParseException(String message, Exception cause) {
        super(message, cause);
    }
}
