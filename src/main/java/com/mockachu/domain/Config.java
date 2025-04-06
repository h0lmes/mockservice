package com.mockachu.domain;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private Settings settings = new Settings();
    private final List<Route> routes = new ArrayList<>();
    private final List<OutboundRequest> requests = new ArrayList<>();
    private final List<ApiTest> tests = new ArrayList<>();
    private final List<Scenario> scenarios = new ArrayList<>();

    public Config() {
        // default
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public List<OutboundRequest> getRequests() {
        return requests;
    }

    public List<ApiTest> getTests() {
        return tests;
    }

    public List<Scenario> getScenarios() {
        return scenarios;
    }
}
