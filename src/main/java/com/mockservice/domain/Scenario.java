package com.mockservice.domain;

import java.util.Objects;

public class Scenario implements Comparable<Scenario> {

    private String group = "";
    private String alias = "";
    private ScenarioType type = ScenarioType.MAP;
    private String data = "";

    public Scenario() {
        // default
    }

    public String getGroup() {
        return group;
    }

    public Scenario setGroup(String group) {
        this.group = group == null ? "" : group;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public Scenario setAlias(String alias) {
        this.alias = alias == null ? "" : alias;
        return this;
    }

    public ScenarioType getType() {
        return type;
    }

    public Scenario setType(ScenarioType type) {
        this.type = type == null ? ScenarioType.MAP : type;
        return this;
    }

    public String getData() {
        return data;
    }

    public Scenario setData(String data) {
        this.data = data;
        return this;
    }

    public void assignFrom(Scenario source) {
        setGroup(source.getGroup());
        setAlias(source.getAlias());
        setType(source.getType());
        setData(source.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Scenario)) return false;
        Scenario other = (Scenario) o;
        return alias.equals(other.getAlias());
    }

    @Override
    public String toString() {
        return String.format("(group=%s, type=%s, alias=%s)", group, type, alias);
    }

    @Override
    public int compareTo(Scenario o) {
        int c;
        c = this.group.compareTo(o.getGroup());
        if (c != 0) return c;
        c = this.alias.compareTo(o.getAlias());
        return c;
    }
}
