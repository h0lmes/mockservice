package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.domain.RouteAlreadyExistsException;
import com.mockservice.repository.ConfigRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
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
    public Optional<String> getRandomAltFor(Route route) {
        Predicate<Route> condition = r -> route.getMethod().equals(r.getMethod())
                && route.getPath().equals(r.getPath())
                && !r.getDisabled();
        List<String> alts = configRepository.findAllRoutes().stream()
                .filter(condition)
                .map(Route::getAlt)
                .collect(Collectors.toList());
        if (alts.isEmpty()) {
            return Optional.empty();
        }
        if (alts.size() == 1) {
            return Optional.of(alts.get(0));
        }
        int index = ThreadLocalRandom.current().nextInt(0, alts.size());
        return Optional.of(alts.get(index));
    }

    @Override
    public List<Route> getRoutesAsList() {
        return new ArrayList<>(configRepository.findAllRoutes());
    }

    @Override
    public synchronized List<Route> putRoute(Route route, Route replacement) throws IOException, RouteAlreadyExistsException {
        configRepository.putRoute(route, replacement);
        return getRoutesAsList();
    }

    @Override
    public synchronized List<Route> putRoutes(List<Route> routes) throws IOException, RouteAlreadyExistsException {
        configRepository.putRoutes(routes);
        return getRoutesAsList();
    }

    @Override
    public synchronized List<Route> deleteRoutes(List<Route> routes) throws IOException {
        configRepository.deleteRoutes(routes);
        return getRoutesAsList();
    }
}
