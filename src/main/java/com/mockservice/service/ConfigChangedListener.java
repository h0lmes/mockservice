package com.mockservice.service;

public interface ConfigChangedListener {
    void onBeforeConfigChanged();
    void onAfterConfigChanged();
}
