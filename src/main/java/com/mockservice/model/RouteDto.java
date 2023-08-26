package com.mockservice.model;

import com.mockservice.domain.RouteType;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Objects;

public class RouteDto {

    private String group = "";
    private RouteType type = RouteType.REST;
    private RequestMethod method = RequestMethod.GET;
    private String path = "";
    private String alt = "";
    private int responseCode = 200;
    private String response = "";
    private String requestBodySchema = "";
    private boolean disabled = false;
    private List<RouteVariable> variables;

    public RouteDto() {
        // default
    }

    public String getGroup() {
        return group;
    }

    public RouteDto setGroup(String group) {
        this.group = group;
        return this;
    }

    public RouteType getType() {
        return type;
    }

    public RouteDto setType(RouteType type) {
        this.type = type;
        return this;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public RouteDto setMethod(RequestMethod method) {
        this.method = method;
        return this;
    }

    public String getPath() {
        return path;
    }

    public RouteDto setPath(String path) {
        this.path = path;
        return this;
    }

    public String getAlt() {
        return alt;
    }

    public RouteDto setAlt(String alt) {
        this.alt = alt;
        return this;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public RouteDto setResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    public String getResponse() {
        return response;
    }

    public RouteDto setResponse(String response) {
        this.response = response;
        return this;
    }

    public String getRequestBodySchema() {
        return requestBodySchema;
    }

    public RouteDto setRequestBodySchema(String requestBodySchema) {
        this.requestBodySchema = requestBodySchema;
        return this;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public RouteDto setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public List<RouteVariable> getVariables() {
        return variables;
    }

    public RouteDto setVariables(List<RouteVariable> variables) {
        this.variables = variables;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path, alt);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RouteDto other) {
            return method.equals(other.getMethod())
                    && path.equals(other.getPath())
                    && alt.equals(other.getAlt());
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("(method=%s, path=%s, alt=%s)", method, path, alt);
    }
}
