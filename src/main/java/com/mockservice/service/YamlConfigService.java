package com.mockservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.mockservice.mockconfig.Config;
import com.mockservice.mockconfig.Route;
import com.mockservice.mockconfig.RouteAlreadyExistsException;
import com.mockservice.mockconfig.RouteType;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class YamlConfigService implements ConfigService {

    private final String defaultConfigPath;
    private final String configPath;
    private Config config;
    private final List<Consumer<Route>> routeCreatedListeners = new ArrayList<>();
    private final List<BiConsumer<Route, Route>> routeUpdatedListeners = new ArrayList<>();
    private final List<Consumer<Route>> routeDeletedListeners = new ArrayList<>();

    public YamlConfigService(@Value("${application.config.default-path}") String defaultConfigPath,
                             @Value("${application.config.path}") String configPath) {
        this.defaultConfigPath = defaultConfigPath;
        this.configPath = configPath;

        try {
            readConfigFromFile();
        } catch (IOException e) {
            try {
                readConfigFromResource();
            } catch (IOException ex) {
                config = new Config();
            }
        }
    }

    private void readConfigFromFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        config = mapper.readValue(getConfigFile(), Config.class);
    }

    private File getConfigFile() {
        return new File(configPath);
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
        Resource resource = resourceLoader.getResource(defaultConfigPath);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            config = mapper.readValue(reader, Config.class);
        }
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
    public Stream<Route> getRoutesDistinctByPathAndMethod(RouteType type) {
        return getEnabledRoutes()
                .filter(route -> type.equals(route.getType()))
                .filter(distinctByKey(r -> r.getMethod().toString() + r.getPath()));
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
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
        saveConfigToFile();
        if (updated) {
            notifyRouteUpdated(route, replacement);
        } else {
            notifyRouteCreated(replacement);
        }
        return getRoutes().collect(Collectors.toList());
    }

    @Override
    public List<Route> deleteRoute(Route route) throws IOException {
        boolean deleted = config.deleteRoute(route);
        saveConfigToFile();
        if (deleted) {
            notifyRouteDeleted(route);
        }
        return getRoutes().collect(Collectors.toList());
    }

    private void notifyRouteCreated(Route route) {
        routeCreatedListeners.forEach(c -> c.accept(route));
    }

    private void notifyRouteUpdated(Route route, Route replacement) {
        routeUpdatedListeners.forEach(c -> c.accept(route, replacement));
    }

    private void notifyRouteDeleted(Route route) {
        routeDeletedListeners.forEach(c -> c.accept(route));
    }

    @Override
    public void registerRouteCreatedListener(Consumer<Route> listener) {
        routeCreatedListeners.add(listener);
    }

    @Override
    public void registerRouteUpdatedListener(BiConsumer<Route, Route> listener) {
        routeUpdatedListeners.add(listener);
    }

    @Override
    public void registerRouteDeletedListener(Consumer<Route> listener) {
        routeDeletedListeners.add(listener);
    }

    @Override
    public void unregisterRouteCreatedListener(Consumer<Route> listener) {
        routeCreatedListeners.remove(listener);
    }

    @Override
    public void unregisterRouteUpdatedListener(BiConsumer<Route, Route> listener) {
        routeUpdatedListeners.remove(listener);
    }

    @Override
    public void unregisterRouteDeletedListener(Consumer<Route> listener) {
        routeDeletedListeners.remove(listener);
    }
}
