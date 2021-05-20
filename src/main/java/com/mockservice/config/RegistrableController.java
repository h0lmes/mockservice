package com.mockservice.config;

import com.mockservice.mockconfig.RouteType;

public interface RegistrableController {
    RouteType getRouteType();
    String getRouteGroup();
}
