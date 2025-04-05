package com.mockservice.exception;

public class HttpServiceException extends RuntimeException {
    private final String message;

    public HttpServiceException(String message, Exception cause) {
        super(message, cause);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return toString();
    }

    @Override
    public String toString() {
        return message + (getCause() == null ? "" : ": " + getCause().getMessage());
    }
}
