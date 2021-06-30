package com.mockservice.repository;

import com.mockservice.domain.Route;

public interface NotifiableRoutesChanged {
    void onRouteCreated(Route route);
    void onRouteDeleted(Route route);
}
