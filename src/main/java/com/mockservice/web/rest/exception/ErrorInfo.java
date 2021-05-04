package com.mockservice.web.rest.exception;

public class ErrorInfo {

    private String type;
    private String message;

    public ErrorInfo(Throwable t) {
        this(t.getClass().getSimpleName(), t.getMessage());
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
