package com.mockachu.model;

import com.mockachu.domain.OutboundRequest;
import com.mockachu.domain.RouteType;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Objects;

public class OutboundRequestDto {

    private String id = "";
    private String group = "";
    private RouteType type = RouteType.REST;
    private RequestMethod method = RequestMethod.GET;
    private String path = "";
    private String headers = "";
    private String body = "";
    private boolean responseToVars = true;
    private boolean disabled = false;
    private boolean triggerRequest = false;
    private String triggerRequestIds = "";

    public OutboundRequestDto() {
        // default constructor
    }

    public String getId() {
        return id;
    }

    public OutboundRequestDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public OutboundRequestDto setGroup(String group) {
        this.group = group;
        return this;
    }

    public RouteType getType() {
        return type;
    }

    public OutboundRequestDto setType(RouteType type) {
        this.type = type;
        return this;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public OutboundRequestDto setMethod(RequestMethod method) {
        this.method = method;
        return this;
    }

    public String getPath() {
        return path;
    }

    public OutboundRequestDto setPath(String path) {
        this.path = path;
        return this;
    }

    public String getHeaders() {
        return headers;
    }

    public OutboundRequestDto setHeaders(String headers) {
        this.headers = headers;
        return this;
    }

    public String getBody() {
        return body;
    }

    public OutboundRequestDto setBody(String body) {
        this.body = body;
        return this;
    }

    public boolean isResponseToVars() {
        return responseToVars;
    }

    public OutboundRequestDto setResponseToVars(boolean responseToVars) {
        this.responseToVars = responseToVars;
        return this;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public OutboundRequestDto setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public boolean isTriggerRequest() {
        return triggerRequest;
    }

    public OutboundRequestDto setTriggerRequest(boolean triggerRequest) {
        this.triggerRequest = triggerRequest;
        return this;
    }

    public String getTriggerRequestIds() {
        return triggerRequestIds;
    }

    public OutboundRequestDto setTriggerRequestIds(String triggerRequestIds) {
        this.triggerRequestIds = triggerRequestIds;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OutboundRequest other)) return false;
        return id.equals(other.getId());
    }

    @Override
    public String toString() {
        return String.format("(id=%s, method=%s, path=%s)", id, method, path);
    }
}
