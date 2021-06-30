package com.mockservice.repository;

public interface NotifiableScenariosChanged {
    void onScenarioUpdated(String oldAlias, String newAlias);
    void onScenarioDeleted(String alias);
}
