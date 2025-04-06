package com.mockachu.exception;

public class ScenarioParseException extends RuntimeException {

    public ScenarioParseException(String message, Exception cause) {
        super(message, cause);
    }
}
