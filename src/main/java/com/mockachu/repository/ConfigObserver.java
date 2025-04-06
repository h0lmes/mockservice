package com.mockachu.repository;

public interface ConfigObserver {
    void onBeforeConfigChanged();
    void onAfterConfigChanged();
}
