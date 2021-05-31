package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.domain.Config;

import java.io.IOException;

public interface ConfigRepository {
    void trySaveConfigToFile() throws IOException;
    Config getConfig();
    String getConfigData() throws JsonProcessingException;
    void writeConfigData(String data) throws IOException;
    void registerConfigChangedListener(ConfigChangedListener listener);
}
