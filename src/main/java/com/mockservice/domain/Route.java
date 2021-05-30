package com.mockservice.domain;

import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Objects;

public class Route implements Comparable<Route> {

    private String group = "";
    private String path = "";
    private RequestMethod method = RequestMethod.GET;
    private RouteType type = RouteType.REST;
    private String suffix = "";
    private String response = "";
    private boolean disabled = false;

    public Route() {
        // default
    }

    public Route(String method, String path) {
        this.method = RequestMethod.valueOf(method);
        this.path = path;
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

    public String getSuffix() {
        return suffix;
    }

    public Route setSuffix(String suffix) {
        this.suffix = suffix == null ? "" : suffix;
        return this;
    }

    public String getResponse() {
        return response;
    }

    public Route setResponse(String response) {
        this.response = response == null ? "" : response;
        return this;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public Route setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public void assignFrom(Route source) {
        setGroup(source.getGroup());
        setPath(source.getPath());
        setMethod(source.getMethod());
        setType(source.getType());
        setSuffix(source.getSuffix());
        setResponse(source.getResponse());
        setDisabled(source.getDisabled());
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path, suffix);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Route)) return false;
        Route other = (Route) o;
        return method.equals(other.getMethod())
                && path.equals(other.getPath())
                && suffix.equals(other.getSuffix());
    }

    @Override
    public String toString() {
        return String.format("(group=%s, type=%s, method=%s, path=%s, suffix=%s, disabled=%s)", group, type, method, path, suffix, disabled);
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
        c = this.suffix.compareTo(o.getSuffix());
        return c;
    }
}
