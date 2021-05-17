package com.mockservice.config;

import com.mockservice.mockconfig.Route;

import java.util.Optional;

public interface RegisteredRoutesHolder {
    Optional<Route> getRegisteredRoute(Route lookFor);
}
