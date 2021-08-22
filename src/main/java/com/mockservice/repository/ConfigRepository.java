package com.mockservice.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.domain.Route;
import com.mockservice.domain.Scenario;
import com.mockservice.domain.Settings;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ConfigRepository {
    String getConfigData() throws JsonProcessingException;
    void writeConfigData(String data) throws IOException;
    void backup() throws IOException;
    void restore() throws IOException;

    Settings getSettings();
    void setSettings(Settings settings) throws IOException;

    List<Route> findAllRoutes();
    Optional<Route> findRoute(Route route);
    void putRoute(Route route) throws IOException;
    void putRoutes(List<Route> routes, boolean overwrite) throws IOException;
    void deleteRoutes(List<Route> routes) throws IOException;

    List<Scenario> findAllScenarios();
    Optional<Scenario> findScenario(Scenario scenario);
    void putScenario(Scenario scenario) throws IOException;
    void deleteScenario(Scenario scenario) throws IOException;

    void registerConfigChangedListener(ConfigChangedListener listener);
    void registerRoutesChangedListener(RoutesChangedListener listener);
    void registerScenariosChangedListener(ScenariosChangedListener listener);
}
