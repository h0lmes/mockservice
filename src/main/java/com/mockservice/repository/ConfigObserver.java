package com.mockservice.repository;

public interface ConfigObserver {
    void onBeforeConfigChanged();
    void onAfterConfigChanged();
}
