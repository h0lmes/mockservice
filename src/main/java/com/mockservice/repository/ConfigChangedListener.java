package com.mockservice.repository;

public interface ConfigChangedListener {
    void onBeforeConfigChanged();
    void onAfterConfigChanged();
}
