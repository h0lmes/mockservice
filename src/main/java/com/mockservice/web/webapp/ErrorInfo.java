package com.mockservice.web.webapp;

public class ErrorInfo {

    private final String type;
    private final String message;

    public static ErrorInfo of(Throwable t) {
        return new ErrorInfo(t.getClass().getSimpleName(), t.getMessage());
    }

    private ErrorInfo(String type, String message) {
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
