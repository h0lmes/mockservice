package com.mockservice.repository;

public interface ScenariosChangedListener {
    void onScenarioUpdated(String oldAlias, String newAlias);
    void onScenarioDeleted(String alias);
}
