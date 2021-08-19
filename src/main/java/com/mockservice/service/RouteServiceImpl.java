package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.util.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RouteServiceImpl implements RouteService {

    private final ConfigRepository configRepository;

    public RouteServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public Optional<Route> getEnabledRoute(Route route) {
        return configRepository
                .findRoute(route)
                .filter(r -> !r.getDisabled());
    }

    @Override
    public Optional<String> getRandomAltFor(RequestMethod method, String path) {
        List<String> alts = configRepository
                .findAllRoutes()
                .stream()
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

    @Override
    public List<Route> getRoutesAsList() {
        return Collections.unmodifiableList(configRepository.findAllRoutes());
    }

    @Override
    public synchronized List<Route> putRoute(Route route) throws IOException {
        configRepository.putRoute(route);
        return getRoutesAsList();
    }

    @Override
    public synchronized List<Route> putRoutes(List<Route> routes, boolean overwrite) throws IOException {
        configRepository.putRoutes(routes, overwrite);
        return getRoutesAsList();
    }

    @Override
    public synchronized List<Route> deleteRoutes(List<Route> routes) throws IOException {
        configRepository.deleteRoutes(routes);
        return getRoutesAsList();
    }
}
