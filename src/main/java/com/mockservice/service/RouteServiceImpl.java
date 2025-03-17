package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.mapper.RouteMapper;
import com.mockservice.model.RouteDto;
import com.mockservice.model.RouteVariable;
import com.mockservice.model.RouteVariableDto;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.template.MockVariables;
import com.mockservice.util.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@Service
public class RouteServiceImpl implements RouteService {

    private final ConfigRepository configRepository;
    private final RouteMapper routeMapper;
    private final RandomUtils randomUtils;

    private final Map<Route, MockVariables> routesVariablesValues = new ConcurrentHashMap<>();

    public RouteServiceImpl(ConfigRepository configRepository,
                            RouteMapper routeMapper,
                            RandomUtils randomUtils
    ) {
        this.configRepository = configRepository;
        this.routeMapper = routeMapper;
        this.randomUtils = randomUtils;
    }

    @Override
    public Optional<Route> getEnabledRoute(Route route) {
        return configRepository
            .findRoute(route)
            .filter(r -> !r.getDisabled());
    }

    @Override
    public Optional<String> getRandomAltFor(RequestMethod method, String path) {
        List<String> alts = configRepository.findAllRoutes().stream()
            .filter(r -> Objects.equals(method, r.getMethod())
                && Objects.equals(path, r.getPath())
                && !r.getDisabled())
            .map(Route::getAlt)
            .toList();
        if (alts.isEmpty()) {
            return Optional.empty();
        }
        if (alts.size() == 1) {
            return Optional.of(alts.get(0));
        }
        return Optional.of(alts.get(randomUtils.rnd(alts.size())));
    }

    @Override
    public Optional<Route> getRouteForVariables(RequestMethod method, String path, MockVariables variables) {
        return configRepository
                .findAllRoutes()
                .stream()
                .filter(r -> Objects.equals(method, r.getMethod())
                        && Objects.equals(path, r.getPath())
                        && !r.getDisabled())
                .filter(r -> r.getMatcher().match(variables))
                .findFirst();
    }

    //----------------------------------------------------------------------------------

    @Override
    public List<RouteDto> getRoutes() {
        BiConsumer<Route, RouteDto> postProcess = (route, dto) -> dto.setVariables(variablesFromRoute(route));
        return routeMapper.toDto(configRepository.findAllRoutes(), postProcess);
    }

    private List<RouteVariable> variablesFromRoute(Route route) {
        List<RouteVariable> routeVariables = configRepository.getRouteVariables(route);
        routeVariables.forEach(v -> {
            MockVariables routeVariablesValues = routesVariablesValues.get(route);
            if (routeVariablesValues != null) {
                v.setValue(routeVariablesValues.get(v.getName()));
            }
        });
        return routeVariables;
    }

    @Override
    public synchronized void putRoute(RouteDto reference, RouteDto route) throws IOException {
        configRepository.putRoute(routeMapper.fromDto(reference), routeMapper.fromDto(route));
    }

    @Override
    public synchronized void putRoutes(List<RouteDto> dto, boolean overwrite) throws IOException {
        configRepository.putRoutes(routeMapper.fromDto(dto), overwrite);
    }

    @Override
    public synchronized void deleteRoutes(List<RouteDto> dto) throws IOException {
        configRepository.deleteRoutes(routeMapper.fromDto(dto));
    }

    @Override
    public MockVariables getRouteVariables(Route route) {
        return routesVariablesValues.get(route);
    }

    @Override
    public RouteVariableDto setRouteVariable(RouteVariableDto variable) {
        Route route = new Route(variable.getMethod(), variable.getPath(), variable.getAlt());
        MockVariables values = routesVariablesValues.computeIfAbsent(route, r -> new MockVariables());
        values.put(variable.getName(), variable.getValue());
        return variable;
    }

    @Override
    public RouteVariableDto clearRouteVariable(RouteVariableDto variable) {
        Route route = new Route(variable.getMethod(), variable.getPath(), variable.getAlt());
        MockVariables values = routesVariablesValues.get(route);
        if (values != null) {
            values.remove(variable.getName());
            if (values.isEmpty()) {
                routesVariablesValues.remove(route);
            }
        }
        return variable.setValue(null);
    }
}
