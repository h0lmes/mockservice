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

    public List<Scenario> getScenarios() {
        return scenarios;
    }
}
