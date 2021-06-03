package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.domain.Route;
import com.mockservice.domain.RouteAlreadyExistsException;
import com.mockservice.domain.Scenario;
import com.mockservice.domain.ScenarioAlreadyExistsException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ConfigRepository {
    String getConfigData() throws JsonProcessingException;
    void writeConfigData(String data) throws IOException;

    List<Route> findAllRoutes();
    Optional<Route> findRoute(Route route);
    void putRoute(Route route, Route replacement) throws RouteAlreadyExistsException, IOException;
    void deleteRoute(Route route) throws IOException;

    List<Scenario> findAllScenarios();
    Optional<Scenario> findScenario(Scenario scenario);
    void putScenario(Scenario scenario, Scenario replacement) throws ScenarioAlreadyExistsException, IOException;
    void deleteScenario(Scenario scenario) throws IOException;

    void registerConfigChangedListener(ConfigChangedListener listener);
    void registerRoutesChangedListener(RoutesChangedListener listener);
    void registerScenariosChangedListener(ScenariosChangedListener listener);
}
