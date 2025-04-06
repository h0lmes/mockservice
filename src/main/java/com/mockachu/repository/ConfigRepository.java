package com.mockachu.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockachu.domain.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ConfigRepository {
    void registerConfigObserver(ConfigObserver observer);
    void registerRouteObserver(RouteObserver observer);
    void registerSettingsObserver(SettingsObserver observer);

    String getConfigData() throws JsonProcessingException;
    void writeConfigData(String data) throws IOException;
    void backup() throws IOException;
    void restore() throws IOException;

    Settings getSettings();
    void setSettings(Settings settings) throws IOException;

    List<Route> findAllRoutes();
    Optional<Route> findRoute(Route route);
    void putRoute(@Nullable Route reference, @Nonnull Route route) throws IOException;
    void putRoutes(List<Route> routes, boolean overwrite) throws IOException;
    void deleteRoutes(List<Route> routes) throws IOException;

    List<OutboundRequest> findAllRequests();
    Optional<OutboundRequest> findRequest(String requestId);
    void putRequest(@Nullable OutboundRequest existing, @Nonnull OutboundRequest request) throws IOException;
    void putRequests(List<OutboundRequest> requests, boolean overwrite) throws IOException;
    void deleteRequests(List<OutboundRequest> requests) throws IOException;

    List<ApiTest> findAllTests();
    Optional<ApiTest> findTest(String alias);
    void putTest(@Nullable ApiTest existing, @Nonnull ApiTest apiTest) throws IOException;
    void putTests(List<ApiTest> apiTests, boolean overwrite) throws IOException;
    void deleteTests(List<ApiTest> apiTests) throws IOException;

    List<Scenario> findAllScenarios();
    Optional<Scenario> findScenario(Scenario scenario);
    void putScenario(@Nullable Scenario reference, @Nonnull Scenario scenario) throws IOException;
    void deleteScenario(Scenario scenario) throws IOException;
}
