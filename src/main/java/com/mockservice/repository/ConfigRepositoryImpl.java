package com.mockservice.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        putRouteInternal(route, false, false);
        config.getRoutes().sort(Route::compareTo);
        trySaveConfigToFile();
    }

    @Override
    public void putRoutes(List<Route> routes, boolean overwrite) throws IOException {
        boolean modified = false;

        for (Route route : routes) {
            modified |= putRouteInternal(route, overwrite, true);
        }

        if (modified) {
            config.getRoutes().sort(Route::compareTo);
            trySaveConfigToFile();
        }
    }

    /**
     * Saves the {@code route} in the repository.
     *
     * @param route the {@code route} to be written to the repository
     * @param overwrite If {@code true} overwrites existing route.
     *                  If a {@code route} is NOT in the repository yet (looking by its {@code id})
     *                  but there is an {@code existingRoute} so that {@code route.equals(existingRoute)}
     *                  then {@code existingRoute} contents (all fields except {@code id})
     *                  will be replaced with those of the {@code route}.
     *
     *                  If a {@code route} IS in the repository (looking by its {@code id})
     *                  and there is an {@code existingRoute} so that {@code route.equals(existingRoute)}
     *                  and not {@code route.getId().equals(existingRoute.getId())}
     *                  then {@code route} contents (all fields except {@code id})
     *                  will be updated instead of throwing an exception.
     *                  This may potentially create a full copy of a route.
     * @param silent If {@code true} silences all exceptions.
     * @return {@code true} when route has been written, {@code false} otherwise
     */
    private boolean putRouteInternal(@Nonnull Route route, boolean overwrite, boolean silent) {
        Objects.requireNonNull(route, "Route could not be null.");

        Route existing = findRouteById(route.getId()).orElse(null);
        if (existing == null) {
            return putNewRouteInternal(route, overwrite, silent);
        } else {
            return putExistingRouteInternal(existing, route, overwrite, silent);
        }
    }

    private boolean putNewRouteInternal(@Nonnull Route route, boolean overwrite, boolean silent) {
        Objects.requireNonNull(route, "Route could not be null.");

        Route existingByEquals = findRoute(route).orElse(null);

        if (existingByEquals == null) {
            config.getRoutes().add(route);
            notifyRouteCreated(route);
        } else {
            if (!overwrite) {
                if (silent) {
                    return false;
                }
                throw new RouteAlreadyExistsException(route);
            }
            notifyRouteDeleted(existingByEquals);
            existingByEquals.assignFrom(route);
            notifyRouteCreated(existingByEquals);
        }
        return true;
    }

    private boolean putExistingRouteInternal(@Nonnull Route existing, Route route, boolean overwrite, boolean silent) {
        Objects.requireNonNull(existing, "Existing route could not be null.");

        if (!overwrite) {
            Route existingByEquals = findRoute(route).orElse(null);
            if (existingByEquals != null && !existingByEquals.getId().equals(route.getId())) {
                if (silent) {
                    return false;
                }
                throw new RouteAlreadyExistsException(route);
            }
        }

        notifyRouteDeleted(existing);
        existing.assignFrom(route);
        notifyRouteCreated(existing);
        return true;
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
        putScenarioInternal(scenario);
        config.getScenarios().sort(Scenario::compareTo);
        trySaveConfigToFile();
    }

    private void putScenarioInternal(Scenario scenario) {
        Scenario existingById = findScenarioById(scenario.getId()).orElse(null);
        Scenario existingByEquals = findScenario(scenario).orElse(null);

        if (existingById == null) {
            if (existingByEquals != null) {
                throw new ScenarioAlreadyExistsException(scenario);
            }

            config.getScenarios().add(scenario);
            notifyScenarioUpdated("", scenario.getAlias());
        } else {
            if (existingByEquals != null && !existingByEquals.getId().equals(scenario.getId())) {
                throw new ScenarioAlreadyExistsException(scenario);
            }

            String oldAlias = existingById.getAlias();
            existingById.assignFrom(scenario);
            notifyScenarioUpdated(oldAlias, existingById.getAlias());
        }
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
