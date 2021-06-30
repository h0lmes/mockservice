package com.mockservice.repository;

public interface NotifiableConfigChanged {
    void onBeforeConfigChanged();
    void onAfterConfigChanged();
}
