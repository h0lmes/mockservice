package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.domain.Scenario;
import com.mockservice.domain.ScenarioParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ActiveScenario {
    private final Scenario scenario;
    private final List<Route> routes = new ArrayList<>();

    public ActiveScenario(Scenario scenario) {
        this.scenario = scenario;
        parse();
    }

    private void parse() {
        List<String> list = scenario.getData().lines().collect(Collectors.toList());
        for (int i = 0; i < list.size(); i++) {
            parseScenarioLine(routes, list.get(i));
        }
    }

    private void parseScenarioLine(List<Route> routes, String s) {
        if (s.trim().isEmpty()) {
            return;
        }

        String[] parts = s.split(";");
        if (parts.length < 2) {
            throw new ScenarioParseException("Error parsing line [" + s + "]", null);
        }

        routes.add(new Route(parts[0], parts[1])
                .setAlt(parts.length > 2 ? parts[2] : ""));
    }

    public Scenario getScenario() {
        return scenario;
    }

    public List<Route> getRoutes() {
        return routes;
    }
}
