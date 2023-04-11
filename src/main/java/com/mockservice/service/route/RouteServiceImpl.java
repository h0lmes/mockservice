package com.mockservice.service.route;

import com.mockservice.domain.Route;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.template.MockVariables;
import com.mockservice.util.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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
            .filter(r -> method.equals(r.getMethod())
                && path.equals(r.getPath())
                && !r.getDisabled())
            .map(Route::getAlt)
            .collect(Collectors.toList());
        if (alts.isEmpty()) {
            return Optional.empty();
        }
        if (alts.size() == 1) {
            return Optional.of(alts.get(0));
        }
        return Optional.of(alts.get(randomUtils.rnd(alts.size())));
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
        Route referenceRoute = routeMapper.fromDto(reference);
        Route newRoute = routeMapper.fromDto(route);
        configRepository.putRoute(referenceRoute, newRoute);
    }

    @Override
    public synchronized void putRoutes(List<RouteDto> dtos, boolean overwrite) throws IOException {
        List<Route> routes = routeMapper.fromDto(dtos);
        configRepository.putRoutes(routes, overwrite);
    }

    @Override
    public synchronized void deleteRoutes(List<RouteDto> dtos) throws IOException {
        List<Route> routes = routeMapper.fromDto(dtos);
        configRepository.deleteRoutes(routes);
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

    @Override
    public MockVariables getRouteVariables(Route route) {
        return routesVariablesValues.get(route);
    }
}
