package com.mockservice.mapper;

import com.mockservice.domain.Route;
import com.mockservice.model.RouteDto;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

public interface RouteMapper {
    RouteDto toDto(Route route, @Nullable BiConsumer<Route, RouteDto> postProcess);
    Route fromDto(RouteDto dto);
    List<RouteDto> toDto(List<Route> routes, @Nullable BiConsumer<Route, RouteDto> postProcess);
    List<Route> fromDto(List<RouteDto> dtos);
}
