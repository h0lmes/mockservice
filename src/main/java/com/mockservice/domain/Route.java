package com.mockservice.domain;

import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Objects;

public class Route implements Comparable<Route> {

    private String group = "";
    private String path = "";
    private RequestMethod method = RequestMethod.GET;
    private RouteType type = RouteType.REST;
    private String alt = "";
    private String response = "";
    private String responseSchema = "";
    private boolean disabled = false;

    public Route() {
        // default
    }

    public Route(String method, String path) {
        this.method = RequestMethod.valueOf(method);
        this.path = path;
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

    public String getPath() {
        return path;
    }

    public Route setPath(String path) {
        this.path = path == null ? "" : path;
        return this;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public Route setMethod(RequestMethod method) {
        this.method = method == null ? RequestMethod.GET : method;
        return this;
    }

    public RouteType getType() {
        return type;
    }

    public Route setType(RouteType type) {
        this.type = type == null ? RouteType.REST : type;
        return this;
    }

    public String getAlt() {
        return alt;
    }

    public Route setAlt(String alt) {
        this.alt = alt == null ? "" : alt;
        return this;
    }

    public String getResponse() {
        return response;
    }

    public Route setResponse(String response) {
        this.response = response == null ? "" : response;
        return this;
    }

    public String getResponseSchema() {
        return responseSchema;
    }

    public Route setResponseSchema(String responseSchema) {
        this.responseSchema = responseSchema == null ? "" : responseSchema;
        return this;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public Route setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public Route assignFrom(Route source) {
        setGroup(source.getGroup());
        setPath(source.getPath());
        setMethod(source.getMethod());
        setType(source.getType());
        setAlt(source.getAlt());
        setResponse(source.getResponse());
        setResponseSchema(source.getResponseSchema());
        setDisabled(source.getDisabled());
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path, alt);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Route)) return false;
        Route other = (Route) o;
        return method.equals(other.getMethod())
                && path.equals(other.getPath())
                && alt.equals(other.getAlt());
    }

    @Override
    public String toString() {
        return String.format("(group=%s, type=%s, method=%s, path=%s, alt=%s, disabled=%s)", group, type, method, path, alt, disabled);
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
