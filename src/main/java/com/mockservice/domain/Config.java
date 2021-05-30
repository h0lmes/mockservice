package com.mockservice.domain;

import java.util.ArrayList;
import java.util.List;

public class Config {

    private List<Route> routes = new ArrayList<>();
    private List<Scenario> scenarios = new ArrayList<>();

    public Config() {
        // default
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public Config setRoutes(List<Route> routes) {
        this.routes = routes;
        return this;
    }

    public List<Scenario> getScenarios() {
        return scenarios;
    }

    public Config setScenarios(List<Scenario> scenarios) {
        this.scenarios = scenarios;
        return this;
    }

    private Route findRoute(Route route) {
        return routes.stream()
                .filter(route::equals)
                .findFirst()
                .orElse(null);
    }

    public boolean putRoute(Route route, Route replacement) throws RouteAlreadyExistsException {
        // do not allow duplicates
        if (!route.equals(replacement)) {
            Route maybeRoute = findRoute(replacement);
            if (maybeRoute != null) {
                throw new RouteAlreadyExistsException(replacement);
            }
        }

        boolean updated = false;
        Route maybeRoute = findRoute(route);
        if (maybeRoute == null) {
            routes.add(replacement);
        } else {
            maybeRoute.assignFrom(replacement);
            updated = true;
        }

        routes.sort(Route::compareTo);
        return updated;
    }

    public boolean deleteRoute(Route route) {
        return routes.remove(route);
    }

    private Scenario findScenario(Scenario scenario) {
        return scenarios.stream()
                .filter(scenario::equals)
                .findFirst()
                .orElse(null);
    }

    public boolean putScenario(Scenario scenario, Scenario replacement) throws ScenarioAlreadyExistsException {
        // do not allow duplicates
        if (!scenario.equals(replacement)) {
            Scenario maybeScenario = findScenario(replacement);
            if (maybeScenario != null) {
                throw new ScenarioAlreadyExistsException(replacement);
            }
        }

        boolean updated = false;
        Scenario maybeScenario = findScenario(scenario);
        if (maybeScenario == null) {
            scenarios.add(replacement);
        } else {
            maybeScenario.assignFrom(replacement);
            updated = true;
        }

        scenarios.sort(Scenario::compareTo);
        return updated;
    }

    public boolean deleteScenario(Scenario scenario) {
        return scenarios.remove(scenario);
    }
}
