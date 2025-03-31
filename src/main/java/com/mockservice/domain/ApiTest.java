package com.mockservice.domain;

import java.util.Objects;

public class ApiTest implements Comparable<ApiTest> {
    private String group = "";
    private String alias = "";
    private String plan = "";

    public ApiTest() {
        // default
    }

    public String getGroup() {
        return group;
    }

    public ApiTest setGroup(String group) {
        this.group = group == null ? "" : group;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public ApiTest setAlias(String alias) {
        this.alias = alias == null ? "" : alias;
        return this;
    }

    public String getPlan() {
        return plan;
    }

    public ApiTest setPlan(String plan) {
        this.plan = plan == null ? "" : plan;
        return this;
    }

    public ApiTest assignFrom(ApiTest source) {
        setGroup(source.getGroup());
        setAlias(source.getAlias());
        setPlan(source.getPlan());
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ApiTest other)) return false;
        return alias.equals(other.getAlias());
    }

    @Override
    public String toString() {
        return String.format("(group=%s, alias=%s)", group, alias);
    }

    @Override
    public int compareTo(ApiTest o) {
        int c;
        c = this.group.compareTo(o.getGroup());
        if (c != 0) return c;
        c = this.alias.compareTo(o.getAlias());
        return c;
    }
}
