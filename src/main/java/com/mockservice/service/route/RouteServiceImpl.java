package com.mockservice.service.route;

import com.mockservice.domain.Route;
import com.mockservice.repository.ConfigObserver;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.repository.RouteObserver;
import com.mockservice.template.TemplateEngine;
import com.mockservice.template.TokenParser;
import com.mockservice.util.Cache;
import com.mockservice.util.HashMapCache;
import com.mockservice.util.RandomUtils;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

@Service
public class RouteServiceImpl implements RouteService, ConfigObserver, RouteObserver {

    private final ConfigRepository configRepository;
    private final RouteMapper routeMapper;
    private final Cache<Route, List<RouteVariable>> routeVariablesCache;
    private final Map<Route, Map<String, String>> routesVariablesValues = new ConcurrentHashMap<>();

    public RouteServiceImpl(ConfigRepository configRepository, TemplateEngine templateEngine, RouteMapper routeMapper) {
        this.configRepository = configRepository;
        this.routeMapper = routeMapper;

        routeVariablesCache = new HashMapCache<>(r ->
            TokenParser.tokenize(r.getResponse()).stream()
                .filter(TokenParser::isToken)
                .map(TokenParser::parseToken)
                .filter(args -> templateEngine != null && !templateEngine.isFunction(args[0]))
                .map(args -> {
                    RouteVariable variable = new RouteVariable().setName(args[0]);
                    if (args.length > 1) {
                        variable.setDefaultValue(args[1]);
                    }
                    return variable;
                })
                .distinct()
                .collect(Collectors.toList())
        );
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
        return Optional.of(alts.get(RandomUtils.rnd(alts.size())));
    }

    //----------------------------------------------------------------------------------

    @Override
    public List<RouteDto> getRoutes() {
        BiConsumer<Route, RouteDto> postProcess = (route, dto) -> dto.setVariables(variablesFromRoute(route));
        return routeMapper.toDto(configRepository.findAllRoutes(), postProcess);
    }

    private List<RouteVariable> variablesFromRoute(Route route) {
        List<RouteVariable> routeVariables = routeVariablesCache.get(route);
        routeVariables.forEach(v -> {
            Map<String, String> routeVariablesValues = routesVariablesValues.get(route);
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
        Map<String, String> values = routesVariablesValues.computeIfAbsent(route, r -> new ConcurrentHashMap<>());
        values.put(variable.getName(), variable.getValue());
        return variable;
    }

    @Override
    public RouteVariableDto clearRouteVariable(RouteVariableDto variable) {
        Route route = new Route(variable.getMethod(), variable.getPath(), variable.getAlt());
        Map<String, String> values = routesVariablesValues.get(route);
        if (values != null) {
            values.remove(variable.getName());
            if (values.isEmpty()) {
                routesVariablesValues.remove(route);
            }
        }
        return variable.setValue(null);
    }

    @Override
    public Map<String, String> getRouteVariables(Route route) {
        return routesVariablesValues.get(route);
    }

    //----------------------------------------------------------------------------------

    @Override
    public void onBeforeConfigChanged() {
        routeVariablesCache.invalidate();
    }

    @Override
    public void onAfterConfigChanged() {
        // ignore
    }

    @Override
    public void onRouteCreated(Route route) {
        // ignore
    }

    @Override
    public void onRouteDeleted(Route route) {
        routeVariablesCache.evict(route);
    }
}
