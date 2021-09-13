package com.mockservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.RequestMethod;

public class Scenario implements Comparable<Scenario> {

    public abstract static class MixInIgnoreIdActive {
        @JsonIgnore
        abstract boolean getActive();
    }

    private String group = "";
    private String alias = "";
    private ScenarioType type = ScenarioType.MAP;
    private String data = "";

    private boolean active = false;
    private final List<Route> routes = new ArrayList<>();

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
        this.data = data == null ? "" : data;
        return this;
    }

    public void assignFrom(Scenario source) {
        setGroup(source.getGroup());
        setAlias(source.getAlias());
        setType(source.getType());
        setData(source.getData());

        if (getActive()) {
            setActive(false);
            setActive(true);
        }
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

    //-----------------------------------------------------------------------------
    //
    //   active
    //
    //-----------------------------------------------------------------------------

    public boolean getActive() {
        return active;
    }

    public Scenario setActive(boolean active) {
        if (active) {
            routes.clear();
            parse();
        } else {
            routes.clear();
        }
        this.active = active;
        return this;
    }

    private void parse() {
        List<String> list = data.lines().collect(Collectors.toList());
        for (int i = 0; i < list.size(); i++) {
            parseLine(routes, list.get(i), i);
        }
    }

    private void parseLine(List<Route> routes, String s, int i) {
        if (!s.trim().isEmpty()) {
            String[] parts = s.split(";");
            if (parts.length < 2) {
                throw new ScenarioParseException("Error parsing scenario line " + i + " [" + s + "]", null);
            }

            routes.add(new Route(parts[0], parts[1], parts.length > 2 ? parts[2] : ""));
        }
    }

    public Optional<String> getAltFor(RequestMethod method, String path) {
        Predicate<Route> condition = r -> method.equals(r.getMethod()) && path.equals(r.getPath());
        return type.getStrategy().apply(routes, condition);
    }
}
