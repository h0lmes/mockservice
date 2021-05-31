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
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class YamlConfigService implements ConfigService {

    private static final Logger log = LoggerFactory.getLogger(YamlConfigService.class);

    private final String resourceConfigPath;
    private final String fileConfigPath;
    private final ObjectReader yamlReader;
    private final ObjectWriter yamlWriter;
    private final List<Consumer<Route>> routeCreatedListeners = new ArrayList<>();
    private final List<Consumer<Route>> routeDeletedListeners = new ArrayList<>();
    private Config config;


    public YamlConfigService(@Value("${application.config.resource}") String resourceConfigPath,
                             @Value("${application.config.file}") String fileConfigPath,
                             YamlReaderWriterService yamlReaderWriterService) {
        this.resourceConfigPath = resourceConfigPath;
        this.fileConfigPath = fileConfigPath;
        this.yamlReader = yamlReaderWriterService.reader();
        this.yamlWriter = yamlReaderWriterService.writer();

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

    private String saveConfigToString() throws JsonProcessingException {
        return yamlWriter.writeValueAsString(config);
    }

    @Override
    public String getConfigData() throws JsonProcessingException {
        return saveConfigToString();
    }

    @Override
    public void writeConfigData(String data) throws IOException {
        unregisterAllRoutes();
        readConfigFromString(data);
        registerAllRoutes();
        trySaveConfigToFile();
    }

    private void unregisterAllRoutes() {
        getRoutes().forEach(this::notifyRouteDeleted);
    }

    private void registerAllRoutes() {
        getRoutes().forEach(this::notifyRouteCreated);
    }

    @Override
    public Stream<Route> getRoutes() {
        return config.getRoutes().stream();
    }

    @Override
    public List<Route> getRoutesAsList() {
        return new ArrayList<>(config.getRoutes());
    }

    @Override
    public Stream<Route> getEnabledRoutes() {
        return getRoutes().filter(route -> !route.getDisabled());
    }

    @Override
    public Optional<Route> getEnabledRoute(RouteType type, RequestMethod method, String path, String suffix) {
        return getEnabledRoutes()
                .filter(route -> type.equals(route.getType())
                        && method.equals(route.getMethod())
                        && path.equals(route.getPath())
                        && suffix.equals(route.getSuffix()))
                .findFirst();
    }

    @Override
    public Optional<Route> getEnabledRoute(Route route) {
        return getEnabledRoute(route.getType(), route.getMethod(), route.getPath(), route.getSuffix());
    }

    @Override
    public List<Route> putRoute(Route route, Route replacement) throws IOException, RouteAlreadyExistsException {
        boolean updated = config.putRoute(route, replacement);
        if (updated) {
            notifyRouteDeleted(route);
        }
        notifyRouteCreated(replacement);
        trySaveConfigToFile();
        return config.getRoutes();
    }

    @Override
    public List<Route> deleteRoute(Route route) throws IOException {
        boolean deleted = config.deleteRoute(route);
        if (deleted) {
            notifyRouteDeleted(route);
            trySaveConfigToFile();
        }
        return config.getRoutes();
    }

    private void notifyRouteCreated(Route route) {
        routeCreatedListeners.forEach(c -> c.accept(route));
    }

    private void notifyRouteDeleted(Route route) {
        routeDeletedListeners.forEach(c -> c.accept(route));
    }

    @Override
    public void registerRouteCreatedListener(Consumer<Route> listener) {
        routeCreatedListeners.add(listener);
    }

    @Override
    public void registerRouteDeletedListener(Consumer<Route> listener) {
        routeDeletedListeners.add(listener);
    }

    @Override
    public List<Scenario> getScenariosAsList() {
        return new ArrayList<>(config.getScenarios());
    }

    @Override
    public List<Scenario> putScenario(Scenario scenario, Scenario replacement) throws IOException, ScenarioAlreadyExistsException {
        config.putScenario(scenario, replacement);
        trySaveConfigToFile();
        return config.getScenarios();
    }

    @Override
    public List<Scenario> deleteScenario(Scenario scenario) throws IOException {
        boolean deleted = config.deleteScenario(scenario);
        if (deleted) {
            trySaveConfigToFile();
        }
        return config.getScenarios();
    }

    private Stream<Scenario> getActiveScenariosAsStream() {
        return config.getScenarios().stream().filter(Scenario::getActive);
    }

    @Override
    public Optional<String> getRouteSuffixFromScenario(RequestMethod method, String path) {
        return getActiveScenariosAsStream()
                .map(s -> s.getSuffix(method, path))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
