package com.mockservice.service;

public interface ScenariosChangedListener {
    void onScenarioUpdated(String oldAlias, String newAlias);
    void onScenarioDeleted(String alias);
}
