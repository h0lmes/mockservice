package com.mockservice.mockconfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Objects;

public class Route {

    private boolean disabled;
    private RouteType type;
    private RequestMethod method;
    private String path;
    private String suffix;
    private String response;
    private String group;

    public Route() {
        // default
    }

    public Route(RouteType type, RequestMethod method, String path) {
        this.type = type;
        this.method = method;
        this.path = path;
    }

    public Route(RouteType type, RequestMethod method, String path, String group) {
        this.type = type;
        this.method = method;
        this.path = path;
        this.group = group;
    }

    public RouteType getType() {
        return type;
    }

    public void setType(RouteType type) {
        this.type = type;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public void setMethod(RequestMethod method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @JsonIgnore
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
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
                && path.equals(other.getPath());
    }

    @Override
    public  String toString() {
        return String.format("(disabled=%s, type=%s, method=%s, path=%s, suffix=%s, group=%s)", disabled, type, method, path, suffix, group);
    }
}
