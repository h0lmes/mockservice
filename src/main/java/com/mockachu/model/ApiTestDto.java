package com.mockachu.model;

import java.util.Objects;

public class ApiTestDto {
    private String group = "";
    private String alias = "";
    private String plan = "";

    public ApiTestDto() {
        // default
    }

    public String getGroup() {
        return group;
    }

    public ApiTestDto setGroup(String group) {
        this.group = group == null ? "" : group;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public ApiTestDto setAlias(String alias) {
        this.alias = alias == null ? "" : alias;
        return this;
    }

    public String getPlan() {
        return plan;
    }

    public ApiTestDto setPlan(String plan) {
        this.plan = plan == null ? "" : plan;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ApiTestDto other)) return false;
        return alias.equals(other.getAlias());
    }

    @Override
    public String toString() {
        return String.format("(group=%s, alias=%s)", group, alias);
    }
}
