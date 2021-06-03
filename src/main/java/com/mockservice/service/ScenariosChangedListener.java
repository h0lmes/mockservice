package com.mockservice.service;

public interface ScenariosChangedListener {
    void onScenarioUpdated(String alias);
    void onScenarioDeleted(String alias);
}
