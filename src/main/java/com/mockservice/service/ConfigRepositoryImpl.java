package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mockservice.domain.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class ConfigRepositoryImpl implements ConfigRepository {

    private static final Logger log = LoggerFactory.getLogger(ConfigRepositoryImpl.class);

    private final String resourceConfigPath;
    private final String fileConfigPath;
    private final ObjectReader yamlReader;
    private final ObjectWriter yamlWriter;
    private final List<ConfigChangedListener> configChangedListeners = new ArrayList<>();
    private Config config;


    public ConfigRepositoryImpl(@Value("${application.config.resource}") String resourceConfigPath,
                                @Value("${application.config.file}") String fileConfigPath,
                                YamlMapperService yamlMapperService) {
        this.resourceConfigPath = resourceConfigPath;
        this.fileConfigPath = fileConfigPath;
        this.yamlReader = yamlMapperService.reader();
        this.yamlWriter = yamlMapperService.writer();

        try {
            readConfigFromFile();
        } catch (IOException e) {
            log.warn("Could not read config from file {}. Falling back to resource.", this.fileConfigPath);
            try {
                readConfigFromResource();
            } catch (IOException ex) {
                log.error("Could not read config from resource {}. Using empty config.", this.resourceConfigPath);
                config = new Config();
            }
        }
    }

    private File getConfigFile() {
        return new File(fileConfigPath);
    }

    private void readConfigFromFile() throws IOException {
        config = yamlReader.readValue(getConfigFile(), Config.class);
    }

    private void readConfigFromResource() throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(resourceConfigPath);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            config = yamlReader.readValue(reader, Config.class);
        }
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public String getConfigData() throws JsonProcessingException {
        return yamlWriter.writeValueAsString(config);
    }

    @Override
    public void writeConfigData(String data) throws IOException {
        notifyBeforeConfigChanged();
        readConfigFromString(data);
        notifyAfterConfigChanged();
        trySaveConfigToFile();
    }

    private void readConfigFromString(String yaml) throws IOException {
        try {
            config = yamlReader.readValue(yaml, Config.class);
        } catch (IOException e) {
            throw new IOException("Could not deserialize config. " + e.getMessage(), e);
        }
    }

    @Override
    public void trySaveConfigToFile() throws IOException {
        try {
            yamlWriter.writeValue(getConfigFile(), config);
        } catch (IOException e) {
            throw new IOException("Could not write config to file. " + e.getMessage(), e);
        }
    }

    private void notifyBeforeConfigChanged() {
        configChangedListeners.forEach(ConfigChangedListener::onBeforeConfigChanged);
    }

    private void notifyAfterConfigChanged() {
        configChangedListeners.forEach(ConfigChangedListener::onAfterConfigChanged);
    }

    @Override
    public void registerConfigChangedListener(ConfigChangedListener listener) {
        configChangedListeners.add(listener);
    }
}
