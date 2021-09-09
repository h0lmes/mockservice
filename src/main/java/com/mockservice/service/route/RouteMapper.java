package com.mockservice.service.route;

import com.mockservice.domain.Route;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;

public interface RouteMapper {
    RouteDto toDto(Route route, @Nullable BiConsumer<Route, RouteDto> postProcess);
    Route fromDto(RouteDto dto);
    List<RouteDto> toDto(List<Route> routes, @Nullable BiConsumer<Route, RouteDto> postProcess);
    List<Route> fromDto(List<RouteDto> dtos);
}
