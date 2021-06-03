package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.domain.RouteAlreadyExistsException;
import com.mockservice.domain.RouteType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RouteServiceImpl implements RouteService {

    private final ConfigRepository configRepository;

    public RouteServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public Optional<Route> getEnabledRoute(Route route) {
        return getEnabledRoute(route.getType(), route.getMethod(), route.getPath(), route.getSuffix());
    }

    @Override
    public Optional<Route> getEnabledRoute(RouteType type, RequestMethod method, String path, String suffix) {
        return configRepository.findAllRoutes().stream()
                .filter(route -> !route.getDisabled()
                        && type.equals(route.getType())
                        && method.equals(route.getMethod())
                        && path.equals(route.getPath())
                        && suffix.equals(route.getSuffix())
                ).findFirst();
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
