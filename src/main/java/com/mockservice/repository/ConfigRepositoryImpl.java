package com.mockservice.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.Config;
import com.mockservice.domain.Route;
import com.mockservice.domain.RouteAlreadyExistsException;
import com.mockservice.domain.Scenario;
import com.mockservice.domain.Settings;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfigRepositoryImpl implements ConfigRepository {

    private static final Logger log = LoggerFactory.getLogger(ConfigRepositoryImpl.class);

    private Config config;
    private final String fileConfigPath;
    private final String fileConfigBackupPath;
    private final ObjectMapper yamlMapper;
    private List<ConfigObserver> configObservers;
    private List<RouteObserver> routeObservers;
    private List<ScenarioObserver> scenarioObservers;

    public ConfigRepositoryImpl(@Value("${application.config-filename}") String fileConfigPath,
                                @Value("${application.config-backup-filename}") String fileConfigBackupPath,
                                @Qualifier("yamlMapper") ObjectMapper yamlMapper) {
        this.fileConfigPath = fileConfigPath;
        this.fileConfigBackupPath = fileConfigBackupPath;
        this.yamlMapper = yamlMapper.copy().setMixIns(getMixIns());

        try {
            readConfigFromFile();
        } catch (IOException e) {
            log.warn("Could not read config file {}. Using empty config.", this.fileConfigPath);
            config = new Config();
        }
    }

    @Autowired(required = false)
    public void setConfigObservers(List<ConfigObserver> configObservers) {
        this.configObservers = configObservers;
    }

    @Autowired(required = false)
    public void setRouteObservers(List<RouteObserver> routeObservers) {
        this.routeObservers = routeObservers;
    }

    @Autowired(required = false)
    public void setScenarioObservers(List<ScenarioObserver> scenarioObservers) {
        this.scenarioObservers = scenarioObservers;
    }

    private Map<Class<?>, Class<?>> getMixIns() {
        Map<Class<?>, Class<?>> mixins = new HashMap<>();
        mixins.put(Scenario.class, Scenario.MixInIgnoreIdActive.class);
        return mixins;
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
    public void putRoute(@Nullable Route reference, @Nonnull Route route) throws IOException {
        Objects.requireNonNull(route, "Route could not be null.");

        putRouteInternal(reference, route);
        config.getRoutes().sort(Route::compareTo);
        trySaveConfigToFile();
    }

    private void putRouteInternal(Route reference, Route route) {
        Route existingReference = null;
        if (reference != null) {
            existingReference = findRoute(reference).orElse(null);
        }
        Route existingRoute = findRoute(route).orElse(null);

        if (existingReference == null) {
            if (existingRoute != null) {
                throw new RouteAlreadyExistsException(route);
            }
            config.getRoutes().add(route);
            notifyRouteCreated(route);
        } else {
            if (existingRoute != null && !existingReference.equals(existingRoute)) {
                throw new RouteAlreadyExistsException(route);
            }
            notifyRouteDeleted(existingReference);
            existingReference.assignFrom(route);
            notifyRouteCreated(existingReference);
        }
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

    private boolean putRouteInternal(@Nonnull Route route, boolean overwrite) {
        Objects.requireNonNull(route, "Route could not be null.");

        Route existing = findRoute(route).orElse(null);

        if (existing == null) {
            return putNewRoute(route);
        } else {
            return putExistingRoute(existing, route, overwrite);
        }
    }

    private boolean putNewRoute(Route route) {
        config.getRoutes().add(route);
        notifyRouteCreated(route);
        return true;
    }

    private boolean putExistingRoute(Route existing, Route route, boolean overwrite) {
        if (overwrite) {
            notifyRouteDeleted(existing);
            existing.assignFrom(route);
            notifyRouteCreated(existing);
            return true;
        }
        return false;
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
        Scenario existing = findScenario(scenario).orElse(null);
        if (existing == null) {
            config.getScenarios().add(scenario);
            notifyScenarioUpdated("", scenario.getAlias());
        } else {
            String oldAlias = existing.getAlias();
            existing.assignFrom(scenario);
            notifyScenarioUpdated(oldAlias, existing.getAlias());
        }
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
    //   Observers
    //
    //----------------------------------------------------------------------

    private void notifyBeforeConfigChanged() {
        if (configObservers != null) {
            configObservers.forEach(ConfigObserver::onBeforeConfigChanged);
        }
    }

    private void notifyAfterConfigChanged() {
        if (configObservers != null) {
            configObservers.forEach(ConfigObserver::onAfterConfigChanged);
        }
    }

    private void notifyRouteCreated(Route route) {
        if (routeObservers != null) {
            routeObservers.forEach(o -> o.onRouteCreated(route));
        }
    }

    private void notifyRouteDeleted(Route route) {
        if (routeObservers != null) {
            routeObservers.forEach(o -> o.onRouteDeleted(route));
        }
    }

    private void notifyScenarioUpdated(String oldAlias, String newAlias) {
        if (scenarioObservers != null) {
            scenarioObservers.forEach(o -> o.onScenarioUpdated(oldAlias, newAlias));
        }
    }

    private void notifyScenarioDeleted(String alias) {
        if (scenarioObservers != null) {
            scenarioObservers.forEach(o -> o.onScenarioDeleted(alias));
        }
    }
}
