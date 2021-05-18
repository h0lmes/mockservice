package com.mockservice.mockconfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Objects;

public class Route {

    private String path = "";
    private RequestMethod method = RequestMethod.GET;
    private RouteType type = RouteType.REST;
    private String suffix = "";
    private String response = "";
    private boolean disabled = false;
    private String group = ""; // non serializable

    public Route() {
        // default
    }

    public String getPath() {
        return path;
    }

    public Route setPath(String path) {
        this.path = path;
        return this;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public Route setMethod(RequestMethod method) {
        this.method = method;
        return this;
    }

    public RouteType getType() {
        return type;
    }

    public Route setType(RouteType type) {
        Objects.requireNonNull(type, "Route type can not be null");
        this.type = type;
        return this;
    }

    public String getSuffix() {
        return suffix;
    }

    public Route setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public String getResponse() {
        return response;
    }

    public Route setResponse(String response) {
        this.response = response;
        return this;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public Route setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    @JsonIgnore
    public String getGroup() {
        return group;
    }

    public Route setGroup(String group) {
        this.group = group;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, method, path);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Route)) return false;
        Route other = (Route) o;
        return type.equals(other.getType())
                && method.equals(other.getMethod())
                && path.equals(other.getPath())
                && suffix.equals(other.getSuffix());
    }

    @Override
    public  String toString() {
        return String.format("(path=%s, method=%s, type=%s, suffix=%s, disabled=%s, group=%s)", path, method, type, suffix, disabled, group);
    }
}
