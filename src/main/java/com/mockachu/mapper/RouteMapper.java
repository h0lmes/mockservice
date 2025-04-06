package com.mockachu.mapper;

import com.mockachu.domain.Route;
import com.mockachu.model.RouteDto;

import java.util.List;

public interface RouteMapper {
    RouteDto toDto(Route route);
    Route fromDto(RouteDto dto);
    List<RouteDto> toDto(List<Route> routes);
    List<Route> fromDto(List<RouteDto> list);
}
