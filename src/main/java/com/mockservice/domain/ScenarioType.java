package com.mockservice.domain;

public enum ScenarioType {

    MAP(new ScenarioMapStrategy()),
    QUEUE(new ScenarioQueueStrategy());

    private final ScenarioStrategy strategy;

    ScenarioType(ScenarioStrategy strategy) {
        this.strategy = strategy;
    }

    public ScenarioStrategy getStrategy() {
        return strategy;
    }
}
