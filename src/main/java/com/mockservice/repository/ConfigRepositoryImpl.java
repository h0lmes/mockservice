package com.mockservice.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConfigRepositoryImpl implements ConfigRepository {

    private static final Logger log = LoggerFactory.getLogger(ConfigRepositoryImpl.class);

    private Config config;
    private final String fileConfigPath;
    private final String fileConfigBackupPath;
    private final ObjectMapper yamlMapper;
    private final List<ConfigChangedListener> configChangedListeners = new ArrayList<>();
    private final List<RoutesChangedListener> routesChangedListeners = new ArrayList<>();
    private final List<ScenariosChangedListener> scenariosChangedListeners = new ArrayList<>();


    public ConfigRepositoryImpl(@Value("${application.config-filename}") String fileConfigPath,
                                @Value("${application.config-backup-filename}") String fileConfigBackupPath,
                                @Qualifier("configYamlMapper") ObjectMapper yamlMapper) {
        this.fileConfigPath = fileConfigPath;
        this.fileConfigBackupPath = fileConfigBackupPath;
        this.yamlMapper = yamlMapper;

        try {
            readConfigFromFile();
        } catch (IOException e) {
            log.warn("Could not read config file {}. Using empty config.", this.fileConfigPath);
            config = new Config();
        }
    }

    private void readConfigFromFile() throws IOException {
        readConfigFromFile(getConfigFile());
    }

    private void readConfigFromFile(File file) throws IOException {
        config = yamlMapper.readValue(file, Config.class);
        if (config == null) {
            throw new IOException("Mapper returned null Config.");
        }
    }

    private void readConfigFromString(String yaml) throws IOException {
        try {
            config = yamlMapper.readValue(yaml, Config.class);
            if (config == null) {
                throw new IOException("Mapper returned null Config.");
            }
        } catch (IOException e) {
            throw new IOException("Could not deserialize config. " + e.getMessage(), e);
        }
    }

    private void trySaveConfigToFile() throws IOException {
        trySaveConfigToFile(getConfigFile());
    }

    private void trySaveConfigToFile(File file) throws IOException {
        try {
            yamlMapper.writeValue(file, config);
        } catch (IOException e) {
            throw new IOException("Could not write config to file. " + e.getMessage(), e);
        }
    }

    private String saveConfigToString() throws JsonProcessingException {
        return yamlMapper.writeValueAsString(config);
    }

    private File getConfigFile() {
        return new File(fileConfigPath);
    }

    private File getConfigBackupFile() {
        return new File(fileConfigBackupPath);
    }

    //----------------------------------------------------------------------
    //
    //   Config
    //
    //----------------------------------------------------------------------

    @Override
    public synchronized String getConfigData() throws JsonProcessingException {
        return saveConfigToString();
    }

    @Override
    public synchronized void writeConfigData(String data) throws IOException {
        notifyBeforeConfigChanged();
        readConfigFromString(data);
        notifyAfterConfigChanged();
        trySaveConfigToFile();
    }

    @Override
    public synchronized void backup() throws IOException {
        trySaveConfigToFile(getConfigBackupFile());
    }

    @Override
    public synchronized void restore() throws IOException {
        notifyBeforeConfigChanged();
        readConfigFromFile(getConfigBackupFile());
        notifyAfterConfigChanged();
        trySaveConfigToFile();
    }

    //----------------------------------------------------------------------
    //
    //   Settings
    //
    //----------------------------------------------------------------------

    @Override
    public synchronized Settings getSettings() {
        return config.getSettings();
    }

    @Override
    public synchronized void setSettings(Settings settings) throws IOException {
        config.setSettings(settings);
        trySaveConfigToFile();
    }

    //----------------------------------------------------------------------
    //
    //   Routes
    //
    //----------------------------------------------------------------------

    @Override
    public List<Route> findAllRoutes() {
        return config.getRoutes();
    }

    @Override
    public Optional<Route> findRoute(Route route) {
        return findAllRoutes().stream()
                .filter(route::equals)
                .findFirst();
    }

    @Override
    public void putRoute(Route route) throws IOException {
        putRouteInternal(route, true);

        config.getRoutes().sort(Route::compareTo);
        trySaveConfigToFile();
    }

    @Override
    public void putRoutes(List<Route> routes, boolean overwrite) throws IOException {
        boolean modified = false;

        for (Route route : routes) {
            modified |= putRouteInternal(route, overwrite);
        }

        if (modified) {
            config.getRoutes().sort(Route::compareTo);
            trySaveConfigToFile();
        }
    }

    private boolean putRouteInternal(Route route, boolean overwrite) {
        Route existingById = findRouteById(route.getId()).orElse(null);
        Route existingByEquals = findRoute(route).orElse(null);

        if (existingById == null) {
            if (existingByEquals != null) {
                throw new RouteAlreadyExistsException(route);
            }

            config.getRoutes().add(route);
            notifyRouteCreated(route);
            return true;
        } else if (overwrite) {
            if (existingByEquals != null && !existingByEquals.getId().equals(route.getId())) {
                throw new RouteAlreadyExistsException(route);
            }

            notifyRouteDeleted(existingById);
            existingById.assignFrom(route);
            notifyRouteCreated(existingById);
            return true;
        }
        return false;
    }

    private Optional<Route> findRouteById(String id) {
        if (id.isEmpty()) {
            return Optional.empty();
        }
        return findAllRoutes().stream()
                .filter(r -> id.equals(r.getId()))
                .findFirst();
    }

    @Override
    public void deleteRoutes(List<Route> routes) throws IOException {
        boolean modified = false;

        for (Route route : routes) {
            if (config.getRoutes().remove(route)) {
                notifyRouteDeleted(route);
                modified = true;
            }
        }

        if (modified) {
            trySaveConfigToFile();
        }
    }

    //----------------------------------------------------------------------
    //
    //   Scenarios
    //
    //----------------------------------------------------------------------

    @Override
    public List<Scenario> findAllScenarios() {
        return config.getScenarios();
    }

    @Override
    public Optional<Scenario> findScenario(Scenario scenario) {
        return findAllScenarios().stream()
                .filter(scenario::equals)
                .findFirst();
    }

    @Override
    public void putScenario(Scenario scenario) throws IOException {
        putScenarioInternal(scenario, true);

        config.getScenarios().sort(Scenario::compareTo);
        trySaveConfigToFile();
    }

    private boolean putScenarioInternal(Scenario scenario, boolean overwrite) {
        Scenario existingById = findScenarioById(scenario.getId()).orElse(null);
        Scenario existingByEquals = findScenario(scenario).orElse(null);

        if (existingById == null) {
            if (existingByEquals != null) {
                throw new ScenarioAlreadyExistsException(scenario);
            }

            config.getScenarios().add(scenario);
            notifyScenarioUpdated("", scenario.getAlias());
            return true;
        } else if (overwrite) {
            if (existingByEquals != null && !existingByEquals.getId().equals(scenario.getId())) {
                throw new ScenarioAlreadyExistsException(scenario);
            }

            String old = existingById.getAlias();
            existingById.assignFrom(scenario);
            notifyScenarioUpdated(old, existingById.getAlias());
            return true;
        }
        return false;
    }

    private Optional<Scenario> findScenarioById(String id) {
        if (id.isEmpty()) {
            return Optional.empty();
        }
        return findAllScenarios().stream()
                .filter(s -> id.equals(s.getId()))
                .findFirst();
    }

    @Override
    public void deleteScenario(Scenario scenario) throws IOException {
        if (config.getScenarios().remove(scenario)) {
            notifyScenarioDeleted(scenario.getAlias());
            trySaveConfigToFile();
        }
    }

    //----------------------------------------------------------------------
    //
    //   Listeners
    //
    //----------------------------------------------------------------------

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

    private void notifyRouteCreated(Route route) {
        routesChangedListeners.forEach(l -> l.onRouteCreated(route));
    }

    private void notifyRouteDeleted(Route route) {
        routesChangedListeners.forEach(l -> l.onRouteDeleted(route));
    }

    @Override
    public void registerRoutesChangedListener(RoutesChangedListener listener) {
        routesChangedListeners.add(listener);
    }

    private void notifyScenarioUpdated(String oldAlias, String newAlias) {
        scenariosChangedListeners.forEach(l -> l.onScenarioUpdated(oldAlias, newAlias));
    }

    private void notifyScenarioDeleted(String alias) {
        scenariosChangedListeners.forEach(l -> l.onScenarioDeleted(alias));
    }

    @Override
    public void registerScenariosChangedListener(ScenariosChangedListener listener) {
        scenariosChangedListeners.add(listener);
    }
}
