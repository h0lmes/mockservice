package com.mockservice.repository;

import com.mockservice.domain.Scenario;

public interface ScenarioObserver {
    void onScenarioCreated(Scenario scenario);
    void onScenarioDeleted(Scenario scenario);
}
