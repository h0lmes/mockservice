package com.mockservice.service.route;

import java.util.Objects;
import org.springframework.web.bind.annotation.RequestMethod;

public class RouteVariableDto implements Comparable<RouteVariableDto> {
    private RequestMethod method = RequestMethod.GET;
    private String path = "";
    private String alt = "";
    private String name;
    private String value;

    public RouteVariableDto() {
    }

    public RequestMethod getMethod() {
        return method;
    }

    public RouteVariableDto setMethod(RequestMethod method) {
        this.method = method;
        return this;
    }

    public String getPath() {
        return path;
    }

    public RouteVariableDto setPath(String path) {
        this.path = path;
        return this;
    }

    public String getAlt() {
        return alt;
    }

    public RouteVariableDto setAlt(String alt) {
        this.alt = alt;
        return this;
    }

    public String getName() {
        return name;
    }

    public RouteVariableDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public RouteVariableDto setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RouteVariableDto)) return false;
        RouteVariableDto other = (RouteVariableDto) o;
        return name.equals(other.getName());
    }

    @Override
    public String toString() {
        return String.format("(name=%s, value=%s)", name, value);
    }

    @Override
    public int compareTo(RouteVariableDto o) {
        return this.name.compareTo(o.getName());
    }
}
