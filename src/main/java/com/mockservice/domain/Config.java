package com.mockservice.domain;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private Settings settings = new Settings();
    private List<Route> routes = new ArrayList<>();
    private List<OutboundRequest> requests = new ArrayList<>();
    private List<Scenario> scenarios = new ArrayList<>();

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

    public List<Scenario> getScenarios() {
        return scenarios;
    }
}
