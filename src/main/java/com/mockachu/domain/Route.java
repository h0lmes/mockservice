package com.mockachu.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Objects;

public class Route implements Comparable<Route> {

    private String group = "";
    private RouteType type = RouteType.REST;
    private RequestMethod method = RequestMethod.GET;
    private String path = "";
    private String alt = "";
    private int responseCode = 200;
    private String response = "";
    private String requestBodySchema = "";
    private boolean disabled = false;
    private boolean triggerRequest = false;
    private String triggerRequestIds = "";
    private String triggerRequestDelay = "";
    // generated
    private VariableMatcher matcher;

    public Route() {
        // default
    }

    public Route(String method, String path, String alt) {
        setMethod(RequestMethod.valueOf(method)).setPath(path).setAlt(alt);
    }

    public Route(Route route) {
        assignFrom(route);
    }

    public String getGroup() {
        return group;
    }

    public Route setGroup(String group) {
        this.group = group == null ? "" : group;
        return this;
    }

    public RouteType getType() {
        return type;
    }

    public Route setType(RouteType type) {
        this.type = type == null ? RouteType.REST : type;
        return this;
    }

    @JsonIgnore
    public boolean isRest() {
        return RouteType.REST.equals(type);
    }

    public RequestMethod getMethod() {
        return method;
    }

    public Route setMethod(RequestMethod method) {
        this.method = method == null ? RequestMethod.GET : method;
        return this;
    }

    public String getPath() {
        return path;
    }

    public Route setPath(String path) {
        this.path = path == null ? "" : path;
        return this;
    }

    public String getAlt() {
        return alt;
    }

    public Route setAlt(String alt) {
        this.alt = alt == null ? "" : alt;
        if (matcher != null) {
            matcher.setCondition(this.alt);
        }
        return this;
    }

    @JsonIgnore
    public VariableMatcher getMatcher() {
        if (matcher == null) {
            matcher = new VariableMatcher(this.alt);
        }
        return matcher;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Route setResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    @JsonIgnore
    public Route setResponseCodeString(String responseCode) {
        try {
            int code = Integer.parseInt(responseCode);
            if (code >= 100 && code <= 599) {
                setResponseCode(code);
            }
        } catch (Exception e) {
            //
        }
        return this;
    }

    public String getResponse() {
        return response;
    }

    public Route setResponse(String response) {
        this.response = response == null ? "" : response;
        return this;
    }

    public String getRequestBodySchema() {
        return requestBodySchema;
    }

    public Route setRequestBodySchema(String requestBodySchema) {
        this.requestBodySchema = requestBodySchema == null ? "" : requestBodySchema;
        return this;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public Route setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public boolean isTriggerRequest() {
        return triggerRequest;
    }

    public Route setTriggerRequest(boolean triggerRequest) {
        this.triggerRequest = triggerRequest;
        return this;
    }

    public String getTriggerRequestIds() {
        return triggerRequestIds;
    }

    public Route setTriggerRequestIds(String triggerRequestIds) {
        this.triggerRequestIds = triggerRequestIds;
        return this;
    }

    public String getTriggerRequestDelay() {
        return triggerRequestDelay;
    }

    public Route setTriggerRequestDelay(String value) {
        this.triggerRequestDelay = value == null ? "" : value;
        return this;
    }

    public Route assignFrom(Route source) {
        setGroup(source.getGroup());
        setPath(source.getPath());
        setMethod(source.getMethod());
        setType(source.getType());
        setAlt(source.getAlt());
        setResponseCode(source.getResponseCode());
        setResponse(source.getResponse());
        setRequestBodySchema(source.getRequestBodySchema());
        setDisabled(source.getDisabled());
        setTriggerRequest(source.isTriggerRequest());
        setTriggerRequestIds(source.getTriggerRequestIds());
        setTriggerRequestDelay(source.getTriggerRequestDelay());
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path, alt);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Route other)) return false;
        return method.equals(other.getMethod())
                && path.equals(other.getPath())
                && alt.equals(other.getAlt());
    }

    @Override
    public String toString() {
        return getMethod() + " " + getPath() + (getAlt().isBlank() ? "" : " " + getAlt());
    }

    @Override
    public int compareTo(Route o) {
        int c;
        c = this.group.compareTo(o.getGroup());
        if (c != 0) return c;
        c = this.type.compareTo(o.getType());
        if (c != 0) return c;
        c = this.method.compareTo(o.getMethod());
        if (c != 0) return c;
        c = this.path.compareTo(o.getPath());
        if (c != 0) return c;
        c = this.alt.compareTo(o.getAlt());
        return c;
    }
}
