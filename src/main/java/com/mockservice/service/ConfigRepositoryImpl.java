package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mockservice.domain.*;
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
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class ConfigRepositoryImpl implements ConfigRepository {

    private static final Logger log = LoggerFactory.getLogger(ConfigRepositoryImpl.class);

    private final String resourceConfigPath;
    private final String fileConfigPath;
    private final ObjectReader yamlReader;
    private final ObjectWriter yamlWriter;
    private Config config;
    private final List<ConfigChangedListener> configChangedListeners = new ArrayList<>();
    private final List<RoutesChangedListener> routesChangedListeners = new ArrayList<>();
    private final List<ScenariosChangedListener> scenariosChangedListeners = new ArrayList<>();


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

    private void readConfigFromFile() throws IOException {
        config = yamlReader.readValue(getConfigFile(), Config.class);
    }

    private File getConfigFile() {
        return new File(fileConfigPath);
    }

    private void readConfigFromResource() throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(resourceConfigPath);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            config = yamlReader.readValue(reader, Config.class);
        }
    }

    private void readConfigFromString(String yaml) throws IOException {
        try {
            config = yamlReader.readValue(yaml, Config.class);
        } catch (IOException e) {
            throw new IOException("Could not deserialize config. " + e.getMessage(), e);
        }
    }

    private void trySaveConfigToFile() throws IOException {
        try {
            yamlWriter.writeValue(getConfigFile(), config);
        } catch (IOException e) {
            throw new IOException("Could not write config to file. " + e.getMessage(), e);
        }
    }

    @Override
    public String getConfigData() throws JsonProcessingException {
        return yamlWriter.writeValueAsString(config);
    }

    @Override
    public synchronized void writeConfigData(String data) throws IOException {
        notifyBeforeConfigChanged();
        readConfigFromString(data);
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
    public void putRoute(Route route, Route replacement) throws RouteAlreadyExistsException, IOException {
        // do not allow duplicates
        if (!route.equals(replacement)) {
            Route maybeRoute = findRoute(replacement).orElse(null);
            if (maybeRoute != null) {
                throw new RouteAlreadyExistsException(replacement);
            }
        }

        boolean updated = false;
        Route maybeRoute = findRoute(route).orElse(null);
        if (maybeRoute == null) {
            config.getRoutes().add(replacement);
        } else {
            maybeRoute.assignFrom(replacement);
            updated = true;
        }

        config.getRoutes().sort(Route::compareTo);

        if (updated) {
            notifyRouteDeleted(route);
        }
        notifyRouteCreated(replacement);
        trySaveConfigToFile();
    }

    @Override
    public void deleteRoute(Route route) throws IOException {
        config.getRoutes().remove(route);
        if (config.getRoutes().remove(route)) {
            notifyRouteDeleted(route);
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
            notifyScenarioUpdated(replacement.getAlias());
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

    private void notifyScenarioUpdated(String alias) {
        scenariosChangedListeners.forEach(l -> l.onScenarioUpdated(alias));
    }

    private void notifyScenarioDeleted(String alias) {
        scenariosChangedListeners.forEach(l -> l.onScenarioDeleted(alias));
    }

    @Override
    public void registerScenariosChangedListener(ScenariosChangedListener listener) {
        scenariosChangedListeners.add(listener);
    }
}
