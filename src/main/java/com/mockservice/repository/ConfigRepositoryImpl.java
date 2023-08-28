package com.mockservice.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.*;
import com.mockservice.model.RouteVariable;
import com.mockservice.template.TemplateEngine;
import com.mockservice.template.TokenParser;
import com.mockservice.util.Cache;
import com.mockservice.util.ConcurrentHashMapCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class ConfigRepositoryImpl implements ConfigRepository {

    private static final Logger log = LoggerFactory.getLogger(ConfigRepositoryImpl.class);

    private Config config;
    private final String fileConfigPath;
    private final String fileConfigBackupPath;
    private final ObjectMapper yamlMapper;
    private List<ConfigObserver> configObservers;
    private List<RouteObserver> routeObservers;
    private final Cache<Route, List<RouteVariable>> routeVariablesCache;

    public ConfigRepositoryImpl(@Value("${application.config-filename}") String fileConfigPath,
                                @Value("${application.config-backup-filename}") String fileConfigBackupPath,
                                @Qualifier("yamlMapper") ObjectMapper yamlMapper,
                                TemplateEngine templateEngine
    ) {
        this.fileConfigPath = fileConfigPath;
        this.fileConfigBackupPath = fileConfigBackupPath;
        this.yamlMapper = yamlMapper.copy().setMixIns(getMixIns());

        routeVariablesCache = new ConcurrentHashMapCache<>(r ->
                TokenParser
                        .tokenize(r.getResponse())
                        .stream()
                        .filter(TokenParser::isToken)
                        .map(TokenParser::parseToken)
                        .filter(args -> !templateEngine.isFunction(args[0]))
                        .map(args -> {
                            RouteVariable variable = new RouteVariable().setName(args[0]);
                            if (args.length > 1) {
                                variable.setDefaultValue(args[1]);
                            }
                            return variable;
                        })
                        .distinct()
                        .toList()
        );

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
        sortRoutes();
        sortScenarios();
    }

    private void sortRoutes() {
        config.getRoutes().sort(Route::compareTo);
    }

    private void sortScenarios() {
        config.getScenarios().sort(Scenario::compareTo);
    }

    private void configFromString(String yaml) throws IOException {
        try {
            config = yamlMapper.readValue(yaml, Config.class);
            if (config == null) {
                throw new IOException("Mapper returned null Config.");
            }
            sortRoutes();
            sortScenarios();
        } catch (IOException e) {
            throw new IOException("Could not deserialize config. " + e.getMessage(), e);
        }
    }

    private void tryPersistConfig() throws IOException {
        tryPersistConfig(getConfigFile());
    }

    private void tryPersistConfig(File file) throws IOException {
        try {
            yamlMapper.writeValue(file, config);
        } catch (IOException e) {
            throw new IOException("Could not write config to file. " + e.getMessage(), e);
        }
    }

    private String configToString() throws JsonProcessingException {
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
        return configToString();
    }

    @Override
    public synchronized void writeConfigData(String data) throws IOException {
        notifyBeforeConfigChanged();
        configFromString(data);
        notifyAfterConfigChanged();
        tryPersistConfig();
    }

    @Override
    public synchronized void backup() throws IOException {
        tryPersistConfig(getConfigBackupFile());
    }

    @Override
    public synchronized void restore() throws IOException {
        notifyBeforeConfigChanged();
        readConfigFromFile(getConfigBackupFile());
        notifyAfterConfigChanged();
        tryPersistConfig();
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
        tryPersistConfig();
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
    public Optional<Route> findRoute(@Nullable Route route) {
        if (route == null) {
            return Optional.empty();
        }
        return findAllRoutes().stream()
                .filter(route::equals)
                .findFirst();
    }

    @Override
    public List<RouteVariable> getRouteVariables(Route route) {
        return routeVariablesCache.get(route);
    }

    @Override
    public void putRoute(@Nullable Route originalRoute, @Nonnull Route route) throws IOException {
        Objects.requireNonNull(route, "Route could not be null.");
        putRouteToConfig(originalRoute, route);
        sortRoutes();
        tryPersistConfig();
    }

    private void putRouteToConfig(Route originalRoute, Route route) {
        Route existingOriginal = findRoute(originalRoute).orElse(null);
        if (existingOriginal == null) {
            putRouteNew(route);
        } else {
            putRouteExisting(route, existingOriginal);
        }
    }

    private void putRouteNew(Route route) {
        Route existing = findRoute(route).orElse(null);
        if (existing != null) {
            throw new RouteAlreadyExistsException(route);
        }
        config.getRoutes().add(route);
        notifyRouteCreated(route);
    }

    private void putRouteExisting(Route route, Route existingOriginal) {
        Route existingRoute = findRoute(route).orElse(null);
        if (existingRoute != null && !existingOriginal.equals(existingRoute)) {
            throw new RouteAlreadyExistsException(route);
        }
        notifyRouteDeleted(existingOriginal);
        existingOriginal.assignFrom(route);
        notifyRouteCreated(existingOriginal);
    }

    @Override
    public void putRoutes(List<Route> routes, boolean overwrite) throws IOException {
        boolean modified = false;

        for (Route route : routes) {
            modified |= putRouteInternal(route, overwrite);
        }

        if (modified) {
            sortRoutes();
            tryPersistConfig();
        }
    }

    private boolean putRouteInternal(@Nonnull Route route, boolean overwrite) {
        Objects.requireNonNull(route, "Route could not be null.");
        Route existing = findRoute(route).orElse(null);
        if (existing == null) {
            putNewRoute(route);
            return true;
        } else if (overwrite) {
            putExistingRoute(existing, route);
            return true;
        }
        return false;
    }

    private void putNewRoute(Route route) {
        config.getRoutes().add(route);
        notifyRouteCreated(route);
    }

    private void putExistingRoute(Route existing, Route route) {
        notifyRouteDeleted(existing);
        existing.assignFrom(route);
        notifyRouteCreated(existing);
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
            tryPersistConfig();
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
    public Optional<Scenario> findScenario(@Nullable Scenario scenario) {
        if (scenario == null) {
            return Optional.empty();
        }
        return findAllScenarios().stream()
                .filter(scenario::equals)
                .findFirst();
    }

    @Override
    public void putScenario(@Nullable Scenario originalScenario, @Nonnull Scenario scenario) throws IOException {
        Objects.requireNonNull(scenario, "Scenario could not be null.");
        putScenarioToConfig(originalScenario, scenario);
        sortScenarios();
        tryPersistConfig();
    }

    private void putScenarioToConfig(Scenario originalScenario, Scenario scenario) {
        Scenario existingOriginal = findScenario(originalScenario).orElse(null);
        if (existingOriginal == null) {
            putScenarioNew(scenario);
        } else {
            putScenarioExisting(scenario, existingOriginal);
        }
    }

    private void putScenarioNew(Scenario scenario) {
        Scenario existing = findScenario(scenario).orElse(null);
        if (existing != null) {
            throw new ScenarioAlreadyExistsException(scenario);
        }
        config.getScenarios().add(scenario);
    }

    private void putScenarioExisting(Scenario scenario, Scenario existingOriginal) {
        Scenario existingScenario = findScenario(scenario).orElse(null);
        if (existingScenario != null && !existingOriginal.equals(existingScenario)) {
            throw new ScenarioAlreadyExistsException(scenario);
        }
        existingOriginal.assignFrom(scenario);
    }

    @Override
    public void deleteScenario(Scenario scenario) throws IOException {
        if (config.getScenarios().remove(scenario)) {
            tryPersistConfig();
        }
    }

    //----------------------------------------------------------------------
    //
    //   Observers
    //
    //----------------------------------------------------------------------

    private void notifyBeforeConfigChanged() {
        routeVariablesCache.invalidate();
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
        routeVariablesCache.evict(route);
        if (routeObservers != null) {
            routeObservers.forEach(o -> o.onRouteDeleted(route));
        }
    }
}
