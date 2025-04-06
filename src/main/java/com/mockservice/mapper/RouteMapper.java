package com.mockservice.mapper;

import com.mockservice.domain.Route;
import com.mockservice.model.RouteDto;

import java.util.List;

public interface RouteMapper {
    RouteDto toDto(Route route);
    Route fromDto(RouteDto dto);
    List<RouteDto> toDto(List<Route> routes);
    List<Route> fromDto(List<RouteDto> list);
}
