package com.mockservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Scenario implements Comparable<Scenario> {

    private String group = "";
    private String alias = "";
    private ScenarioType type = ScenarioType.MAP;
    private String scenario = "";

    private transient boolean active = false;
    private transient List<Route> routes;

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

    public String getScenario() {
        return scenario;
    }

    public Scenario setScenario(String scenario) {
        this.scenario = scenario;
        return this;
    }

    @JsonIgnore
    public boolean getActive() {
        return active;
    }

    public void activate() throws ScenarioParseException {
        parse();
        active = true;
    }

    public void deactivate() {
        active = false;
        cleanup();
    }

    private synchronized void parse() throws ScenarioParseException {
        routes = new ArrayList<>();
        List<String> list = scenario.lines().collect(Collectors.toList());
        for (int i = 0; i < list.size(); i++) {
            try {
                parseScenarioLine(list.get(i));
            } catch (Exception e) {
                throw new ScenarioParseException("Error while parsing scenario [" + alias + "] at line " + i, e);
            }
        }
    }

    private void parseScenarioLine(String s) {
        String[] parts = s.split("\\s+");
        if (parts.length >= 2) {
            String suffix = parts.length > 2 ? parts[2] : "";
            routes.add(
                    new Route(parts[0], parts[1])
                            .setSuffix(suffix)
            );
        }
    }

    private void cleanup() {
        routes.clear();
    }

    public Optional<String> getSuffix(RequestMethod method, String path) {
        if (!active) {
            throw new UnsupportedOperationException("Attempt to use an inactive scenario.");
        }

        Predicate<Route> condition = r -> method.equals(r.getMethod()) && path.equals(r.getPath());
        return type.getStrategy().apply(routes, condition);
    }

    public void assignFrom(Scenario source) {
        deactivate();
        setGroup(source.getGroup());
        setAlias(source.getAlias());
        setType(source.getType());
        setScenario(source.getScenario());
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
        return String.format("(group=%s, type=%s, alias=%s, active=%s)", group, type, alias, active);
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
