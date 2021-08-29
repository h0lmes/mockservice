package com.mockservice.repository;

public interface ScenarioObserver {
    void onScenarioUpdated(String oldAlias, String newAlias);
    void onScenarioDeleted(String alias);
}
