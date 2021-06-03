package com.mockservice.service;

import com.mockservice.domain.Route;

public interface RoutesChangedListener {
    void onRouteCreated(Route route);
    void onRouteDeleted(Route route);
}
