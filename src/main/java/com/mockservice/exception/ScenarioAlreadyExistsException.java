package com.mockservice.exception;

import com.mockservice.domain.Scenario;

public class ScenarioAlreadyExistsException extends RuntimeException {

    private final transient Scenario scenario;

    public ScenarioAlreadyExistsException(Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public String getMessage() {
        return "Scenario already exists: " + scenario;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
