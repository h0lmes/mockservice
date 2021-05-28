package com.mockservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.mockservice.mockconfig.Config;
import com.mockservice.mockconfig.Route;
import com.mockservice.mockconfig.RouteAlreadyExistsException;
import com.mockservice.mockconfig.RouteType;
import com.mockservice.service.model.PlainConfig;
import com.mockservice.util.ReaderWriter;
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
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class YamlConfigService implements ConfigService {

    private static final Logger log = LoggerFactory.getLogger(YamlConfigService.class);

    private final String resourceConfigPath;
    private final String fileConfigPath;
    private Config config;
    private final List<Consumer<Route>> routeCreatedListeners = new ArrayList<>();
    private final List<Consumer<Route>> routeDeletedListeners = new ArrayList<>();


    public YamlConfigService(@Value("${application.config.resource}") String resourceConfigPath,
                             @Value("${application.config.file}") String fileConfigPath) {
        this.resourceConfigPath = resourceConfigPath;
        this.fileConfigPath = fileConfigPath;
        readConfig();
    }

    private void readConfig() {
        try {
            readConfigFromFile();
        } catch (IOException e) {
            log.warn("Could not read config from file {}. Falling back to resource.", fileConfigPath);
            try {
                readConfigFromResource();
            } catch (IOException ex) {
                log.warn("Could not read config from resource {}. Using empty config.", resourceConfigPath);
                config = new Config();
            }
        }
    }

    private File getConfigFile() {
        return new File(fileConfigPath);
    }

    private void readConfigFromFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        config = mapper.readValue(getConfigFile(), Config.class);
    }

    private void saveConfigToFile() throws IOException {
        YAMLFactory factory = new YAMLFactory();
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        factory.enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE);
        factory.disable(YAMLGenerator.Feature.INDENT_ARRAYS);
        factory.disable(YAMLGenerator.Feature.SPLIT_LINES);
        ObjectMapper mapper = new ObjectMapper(factory);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.writeValue(getConfigFile(), config);
    }

    private void readConfigFromResource() throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(resourceConfigPath);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            config = mapper.readValue(reader, Config.class);
        }
    }

    @Override
    public PlainConfig getConfigData() {
        try {
            return new PlainConfig()
                    .setData(ReaderWriter.asString(getConfigFile()))
                    .setFile(true);
        } catch (IOException e) {
            log.warn("Could not read config from file {}. Falling back to resource.", fileConfigPath);
            try {
                return new PlainConfig()
                        .setData(ReaderWriter.asString(resourceConfigPath))
                        .setResource(true);
            } catch (IOException ex) {
                log.warn("Could not read config from resource {}. Using empty config.", resourceConfigPath);
                return new PlainConfig();
            }
        }
    }

    @Override
    public void writeConfigData(PlainConfig config) throws IOException {
        try {
            ReaderWriter.writeFile(getConfigFile(), config.getData());
            log.info("Written config to file {}.", fileConfigPath);
        } catch (IOException e) {
            throw new IOException("Could not write config to file. " + e.getMessage(), e);
        }
        unregisterAllRoutes();
        readConfig();
        registerAllRoutes();
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
    public Stream<Route> getEnabledRoutes() {
        return getRoutes()
                .filter(route -> !route.getDisabled());
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
        saveConfigToFile();
        return config.getRoutes();
    }

    @Override
    public List<Route> deleteRoute(Route route) throws IOException {
        boolean deleted = config.deleteRoute(route);
        if (deleted) {
            notifyRouteDeleted(route);
            saveConfigToFile();
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
}
