package com.mockservice.service.route;

import com.mockservice.domain.Route;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Component
public class RouteMapperImpl implements RouteMapper {

    @Override
    public RouteDto toDto(Route route, @Nullable BiConsumer<Route, RouteDto> postProcess) {
        RouteDto dto = new RouteDto()
            .setGroup(route.getGroup())
            .setType(route.getType())
            .setMethod(route.getMethod())
            .setPath(route.getPath())
            .setAlt(route.getAlt())
            .setResponseCode(route.getResponseCode())
            .setResponse(route.getResponse())
            .setRequestBodySchema(route.getRequestBodySchema())
            .setDisabled(route.getDisabled());
        if (postProcess != null) {
            postProcess.accept(route, dto);
        }
        return dto;
    }

    @Override
    public Route fromDto(RouteDto dto) {
        return new Route()
            .setGroup(dto.getGroup())
            .setType(dto.getType())
            .setMethod(dto.getMethod())
            .setPath(dto.getPath())
            .setAlt(dto.getAlt())
            .setResponseCode(dto.getResponseCode())
            .setResponse(dto.getResponse())
            .setRequestBodySchema(dto.getRequestBodySchema())
            .setDisabled(dto.getDisabled());
    }

    @Override
    public List<RouteDto> toDto(List<Route> routes, @Nullable BiConsumer<Route, RouteDto> postProcess) {
        return routes.stream().map(r -> toDto(r, postProcess)).collect(Collectors.toList());
    }

    @Override
    public List<Route> fromDto(List<RouteDto> dtos) {
        return dtos.stream().map(this::fromDto).collect(Collectors.toList());
    }
}
