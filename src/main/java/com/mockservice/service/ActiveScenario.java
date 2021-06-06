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

    public ActiveScenario(Scenario scenario) throws ScenarioParseException {
        this.scenario = scenario;
        parse();
    }

    private void parse() throws ScenarioParseException {
        List<String> list = scenario.getData().lines().collect(Collectors.toList());
        for (int i = 0; i < list.size(); i++) {
            try {
                parseScenarioLine(routes, list.get(i));
            } catch (Exception e) {
                throw new ScenarioParseException("Error while parsing scenario [" + scenario.getAlias() + "] at line " + i, e);
            }
        }
    }

    private void parseScenarioLine(List<Route> routes, String s) throws ScenarioParseException {
        if (s.trim().isEmpty()) {
            return;
        }

        String[] parts = s.split("\\s+");
        if (parts.length < 2) {
            throw new ScenarioParseException("Error parsing line [" + s + "]", null);
        }

        String suffix = "";
        int i = 2;
        while (i < parts.length) {
            suffix += parts[i++];
        }

        routes.add(new Route(parts[0], parts[1]).setSuffix(suffix));
    }

    public Scenario getScenario() {
        return scenario;
    }

    public List<Route> getRoutes() {
        return routes;
    }
}
