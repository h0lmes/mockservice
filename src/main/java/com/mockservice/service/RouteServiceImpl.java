package com.mockservice.service;

import com.mockservice.domain.Config;
import com.mockservice.domain.Route;
import com.mockservice.domain.RouteAlreadyExistsException;
import com.mockservice.domain.RouteType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
public class RouteServiceImpl implements RouteService, ConfigChangedListener {

    private final ConfigRepository configRepository;
    private final List<Consumer<Route>> routeCreatedListeners = new ArrayList<>();
    private final List<Consumer<Route>> routeDeletedListeners = new ArrayList<>();

    public RouteServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
        configRepository.registerConfigChangedListener(this);
    }

    @Override
    public void onBeforeConfigChanged() {
        getRoutes().forEach(this::notifyRouteDeleted);
    }

    @Override
    public void onAfterConfigChanged() {
        getRoutes().forEach(this::notifyRouteCreated);
    }

    private Config config() {
        return configRepository.getConfig();
    }

    @Override
    public Stream<Route> getRoutes() {
        return config().getRoutes().stream();
    }

    @Override
    public List<Route> getRoutesAsList() {
        return new ArrayList<>(config().getRoutes());
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
    public synchronized List<Route> putRoute(Route route, Route replacement) throws IOException, RouteAlreadyExistsException {
        boolean updated = config().putRoute(route, replacement);
        if (updated) {
            notifyRouteDeleted(route);
        }
        notifyRouteCreated(replacement);
        configRepository.trySaveConfigToFile();
        return config().getRoutes();
    }

    @Override
    public synchronized List<Route> deleteRoute(Route route) throws IOException {
        boolean deleted = config().deleteRoute(route);
        if (deleted) {
            notifyRouteDeleted(route);
            configRepository.trySaveConfigToFile();
        }
        return config().getRoutes();
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
