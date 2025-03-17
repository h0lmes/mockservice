package com.mockservice.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.domain.OutboundRequest;
import com.mockservice.domain.Route;
import com.mockservice.domain.Scenario;
import com.mockservice.domain.Settings;
import com.mockservice.model.RouteVariable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    List<RouteVariable> getRouteVariables(Route route);
    void putRoute(@Nullable Route reference, @Nonnull Route route) throws IOException;
    void putRoutes(List<Route> routes, boolean overwrite) throws IOException;
    void deleteRoutes(List<Route> routes) throws IOException;

    List<OutboundRequest> findAllRequests();
    Optional<OutboundRequest> findRequest(String requestId);
    List<RouteVariable> getRequestVariables(String requestId);
    void putRequest(@Nullable OutboundRequest existing, @Nonnull OutboundRequest request) throws IOException;
    void putRequests(List<OutboundRequest> requests, boolean overwrite) throws IOException;
    void deleteRequests(List<OutboundRequest> requests) throws IOException;

    List<Scenario> findAllScenarios();
    Optional<Scenario> findScenario(Scenario scenario);
    void putScenario(@Nullable Scenario reference, @Nonnull Scenario scenario) throws IOException;
    void deleteScenario(Scenario scenario) throws IOException;
}
