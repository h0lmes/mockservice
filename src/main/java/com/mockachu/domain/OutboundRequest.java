package com.mockachu.domain;

import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Objects;

public class OutboundRequest implements Comparable<OutboundRequest> {

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

    public OutboundRequest() {
        // default constructor
    }

    public String getId() {
        return id;
    }

    public OutboundRequest setId(String id) {
        this.id = id
                .replace('/', '.')
                .replace(':', '.')
                .replace('?', '.')
                .replace('&', '.')
                .replace('%', '.')
                .replace('=', '.')
                .replace(',', '.')
                .replace('$', '.')
                .replace('{', '.')
                .replace('}', '.');
        return this;
    }

    public String generateId() {
        String result = group.isBlank() ? "" : group + ".";
        result += method.name() + ".";
        String pathNoSchema = path.contains("://") ? path.substring(path.indexOf("://") + 3) : path;
        result += pathNoSchema
                .replace('/', '.')
                .replace(':', '.')
                .replace('?', '.')
                .replace('&', '.')
                .replace('%', '.')
                .replace('=', '.')
                .replace(',', '.')
                .replace('$', '.')
                .replace('{', '.')
                .replace('}', '.');
        return result;
    }

    public String getGroup() {
        return group;
    }

    public OutboundRequest setGroup(String value) {
        this.group = value == null ? "" : value;
        return this;
    }

    public RouteType getType() {
        return type;
    }

    public OutboundRequest setType(RouteType value) {
        this.type = value == null ? RouteType.REST : value;
        return this;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public OutboundRequest setMethod(RequestMethod value) {
        this.method = value == null ? RequestMethod.GET : value;
        return this;
    }

    public String getPath() {
        return path;
    }

    public OutboundRequest setPath(String value) {
        this.path = value == null ? "" : value;
        return this;
    }

    public String getHeaders() {
        return headers;
    }

    public OutboundRequest setHeaders(String value) {
        this.headers = value == null ? "" : value;
        return this;
    }

    public String getBody() {
        return body;
    }

    public OutboundRequest setBody(String value) {
        this.body = value == null ? "" : value;
        return this;
    }

    public boolean isResponseToVars() {
        return responseToVars;
    }

    public OutboundRequest setResponseToVars(boolean responseToVars) {
        this.responseToVars = responseToVars;
        return this;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public OutboundRequest setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public boolean isTriggerRequest() {
        return triggerRequest;
    }

    public OutboundRequest setTriggerRequest(boolean triggerRequest) {
        this.triggerRequest = triggerRequest;
        return this;
    }

    public String getTriggerRequestIds() {
        return triggerRequestIds;
    }

    public OutboundRequest setTriggerRequestIds(String value) {
        this.triggerRequestIds = value == null ? "" : value;
        return this;
    }

    public OutboundRequest assignFrom(OutboundRequest source) {
        setId(source.getId());
        setGroup(source.getGroup());
        setType(source.getType());
        setMethod(source.getMethod());
        setPath(source.getPath());
        setHeaders(source.getHeaders());
        setBody(source.getBody());
        setResponseToVars(source.isResponseToVars());
        setDisabled(source.isDisabled());
        setTriggerRequest(source.isTriggerRequest());
        setTriggerRequestIds(source.getTriggerRequestIds());
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

    @Override
    public int compareTo(OutboundRequest o) {
        int c = this.group.compareTo(o.getGroup());
        if (c != 0) return c;
        c = this.type.compareTo(o.getType());
        if (c != 0) return c;
        c = this.method.compareTo(o.getMethod());
        if (c != 0) return c;
        c = this.path.compareTo(o.getPath());
        if (c != 0) return c;
        c = this.id.compareTo(o.getId());
        return c;
    }
}
