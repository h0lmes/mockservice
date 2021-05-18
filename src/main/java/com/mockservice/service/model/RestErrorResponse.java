package com.mockservice.service.model;

public class RestErrorResponse {

    private String type;
    private String message;

    public RestErrorResponse(Throwable t) {
        this(t.getClass().getSimpleName(), t.getMessage());
    }

    public RestErrorResponse(String type, String message) {
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
