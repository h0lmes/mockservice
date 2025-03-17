package com.mockservice.web.webapp;

public class OutboundRequestExecuteRequest {
    private String requestId;

    public OutboundRequestExecuteRequest() {
        // default constructor
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "OutboundRequestExecuteRequest{" +
                "requestId='" + requestId + '\'' +
                '}';
    }
}
