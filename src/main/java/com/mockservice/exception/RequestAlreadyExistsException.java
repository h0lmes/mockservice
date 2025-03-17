package com.mockservice.exception;

import com.mockservice.domain.OutboundRequest;

public class RequestAlreadyExistsException extends RuntimeException {

    private final transient OutboundRequest request;

    public RequestAlreadyExistsException(OutboundRequest request) {
        this.request = request;
    }

    @Override
    public String getMessage() {
        return "Request already exists: " + request;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
