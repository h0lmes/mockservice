package com.mockachu.repository;

import com.mockachu.domain.Route;

public interface RouteObserver {
    void onRouteCreated(Route route);
    void onRouteDeleted(Route route);
}
