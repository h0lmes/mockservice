package com.mockachu.service;

import com.mockachu.domain.Route;
import com.mockachu.domain.RouteType;
import com.mockachu.mapper.RouteMapper;
import com.mockachu.model.RouteDto;
import com.mockachu.repository.ConfigRepository;
import com.mockachu.template.MockVariables;
import com.mockachu.util.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class RouteServiceImpl implements RouteService {

    private final ConfigRepository configRepository;
    private final RouteMapper routeMapper;
    private final RandomUtils randomUtils;

    public RouteServiceImpl(ConfigRepository configRepository,
                            RouteMapper routeMapper,
                            RandomUtils randomUtils) {
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
    public Optional<RouteType> getEnabledRouteType(RequestMethod method, String path) {
        for (var route : configRepository.findAllRoutes()) {
            if (Objects.equals(method, route.getMethod())
                    && Objects.equals(path, route.getPath())
                    && !route.getDisabled()) return Optional.of(route.getType());
        }
        return Optional.empty();
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
    public Optional<String> getAltFor(RequestMethod method, String path, MockVariables variables) {
        for (var route : configRepository.findAllRoutes()) {
            if (Objects.equals(method, route.getMethod())
                    && Objects.equals(path, route.getPath())
                    && !route.getDisabled()
                    && route.getMatcher().match(variables)) return Optional.of(route.getAlt());
        }
        return Optional.empty();
    }

    @Override
    public List<RouteDto> getRoutes() {
        return routeMapper.toDto(configRepository.findAllRoutes());
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
}
