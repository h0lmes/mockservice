package com.mockservice.service.route;

import java.util.Objects;

public class RouteVariable implements Comparable<RouteVariable> {

    private String name;
    private String defaultValue;
    private String value;

    public RouteVariable() {
        // default
    }

    public String getName() {
        return name;
    }

    public RouteVariable setName(String name) {
        this.name = name;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public RouteVariable setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getValue() {
        return value;
    }

    public RouteVariable setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RouteVariable)) return false;
        RouteVariable other = (RouteVariable) o;
        return name.equals(other.getName());
    }

    @Override
    public String toString() {
        return String.format("(name=%s, default=%s, value=%s)", name, defaultValue, value);
    }

    @Override
    public int compareTo(RouteVariable o) {
        return this.name.compareTo(o.getName());
    }
}
