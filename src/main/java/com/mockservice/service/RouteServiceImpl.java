package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.domain.RouteAlreadyExistsException;
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
    public Optional<String> getRandomSuffixFor(Route route) {
        Predicate<Route> condition = r -> route.getMethod().equals(r.getMethod()) && route.getPath().equals(r.getPath());
        List<String> suffixes = configRepository.findAllRoutes().stream()
                .filter(condition)
                .map(Route::getSuffix)
                .collect(Collectors.toList());
        if (suffixes.isEmpty()) {
            return Optional.empty();
        }
        int index = ThreadLocalRandom.current().nextInt(0, suffixes.size());
        return Optional.of(suffixes.get(index));
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
    public synchronized List<Route> deleteRoute(Route route) throws IOException {
        configRepository.deleteRoute(route);
        return getRoutesAsList();
    }
}
