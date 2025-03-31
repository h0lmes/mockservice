package com.mockservice.domain;

public enum ScenarioType {

    MAP(new MapScenarioStrategy()),
    QUEUE(new QueueScenarioStrategy()),
    RING(new CircularQueueScenarioStrategy());

    private final ScenarioStrategy strategy;

    ScenarioType(ScenarioStrategy strategy) {
        this.strategy = strategy;
    }

    public ScenarioStrategy getStrategy() {
        return strategy;
    }
}
