package com.mockservice.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
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

    private final String fileConfigPath;
    private final String fileConfigBackupPath;
    private final ObjectReader yamlReader;
    private final ObjectWriter yamlWriter;
    private Config config;
    private final List<ConfigChangedListener> configChangedListeners = new ArrayList<>();
    private final List<RoutesChangedListener> routesChangedListeners = new ArrayList<>();
    private final List<ScenariosChangedListener> scenariosChangedListeners = new ArrayList<>();


    public ConfigRepositoryImpl(@Value("${application.config-filename}") String fileConfigPath,
                                @Value("${application.config-backup-filename}") String fileConfigBackupPath,
                                @Qualifier("yamlMapper") ObjectMapper yamlMapper) {
        this.fileConfigPath = fileConfigPath;
        this.fileConfigBackupPath = fileConfigBackupPath;
        this.yamlReader = yamlMapper.reader();
        this.yamlWriter = yamlMapper.writer();

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
        config = yamlReader.readValue(file, Config.class);
    }

    private void readConfigFromString(String yaml) throws IOException {
        try {
            config = yamlReader.readValue(yaml, Config.class);
        } catch (IOException e) {
            throw new IOException("Could not deserialize config. " + e.getMessage(), e);
        }
    }

    private void trySaveConfigToFile() throws IOException {
        trySaveConfigToFile(getConfigFile());
    }

    private void trySaveConfigToFile(File file) throws IOException {
        try {
            yamlWriter.writeValue(file, config);
        } catch (IOException e) {
            throw new IOException("Could not write config to file. " + e.getMessage(), e);
        }
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
        return yamlWriter.writeValueAsString(config);
    }

    @Override
    public synchronized void writeConfigData(String data) throws IOException {
        notifyBeforeConfigChanged();
        readConfigFromString(data);
        notifyAfterConfigChanged();
        trySaveConfigToFile();
    }

    @Override
    public void backup() throws IOException {
        trySaveConfigToFile(getConfigBackupFile());
    }

    @Override
    public void restore() throws IOException {
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
    public Settings getSettings() {
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
        return config.getRoutes().stream()
                .filter(route::equals)
                .findFirst();
    }

    @Override
    public void putRoute(Route route) throws IOException {
        Route existingRoute = findRoute(route).orElse(null);

        if (existingRoute == null) {
            config.getRoutes().add(route);
            notifyRouteCreated(route);
        } else {
            notifyRouteDeleted(existingRoute);
            existingRoute.assignFrom(route);
            notifyRouteCreated(route);
        }

        config.getRoutes().sort(Route::compareTo);
        trySaveConfigToFile();
    }

    @Override
    public void putRoutes(List<Route> routes, boolean overwrite) throws IOException {
        boolean modified = false;

        for (Route route : routes) {
            Route existingRoute = findRoute(route).orElse(null);

            if (existingRoute == null) {
                config.getRoutes().add(route);
                notifyRouteCreated(route);
                modified = true;
            } else {
                if (overwrite) {
                    notifyRouteDeleted(existingRoute);
                    existingRoute.assignFrom(route);
                    notifyRouteCreated(route);
                    modified = true;
                }
            }
        }

        if (modified) {
            config.getRoutes().sort(Route::compareTo);
            trySaveConfigToFile();
        }
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
        return config.getScenarios().stream()
                .filter(scenario::equals)
                .findFirst();
    }

    @Override
    public void putScenario(Scenario scenario, Scenario replacement) throws ScenarioAlreadyExistsException, IOException {
        // do not allow duplicates
        if (!scenario.equals(replacement)) {
            Scenario maybeScenario = findScenario(replacement).orElse(null);
            if (maybeScenario != null) {
                throw new ScenarioAlreadyExistsException(replacement);
            }
        }

        boolean updated = false;
        Scenario maybeScenario = findScenario(scenario).orElse(null);
        if (maybeScenario == null) {
            config.getScenarios().add(replacement);
        } else {
            maybeScenario.assignFrom(replacement);
            updated = true;
        }

        config.getScenarios().sort(Scenario::compareTo);

        if (updated) {
            notifyScenarioUpdated(scenario.getAlias(), replacement.getAlias());
        } else {
            notifyScenarioUpdated("", replacement.getAlias());
        }
        trySaveConfigToFile();
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
