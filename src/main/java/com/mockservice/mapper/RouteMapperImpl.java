package com.mockservice.mapper;

import com.mockservice.domain.Route;
import com.mockservice.model.RouteDto;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

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
                .setDisabled(route.getDisabled())
                .setTriggerRequest(route.isTriggerRequest())
                .setTriggerRequestIds(route.getTriggerRequestIds());
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
                .setDisabled(dto.getDisabled())
                .setTriggerRequest(dto.isTriggerRequest())
                .setTriggerRequestIds(dto.getTriggerRequestIds());
    }

    @Override
    public List<RouteDto> toDto(List<Route> routes, @Nullable BiConsumer<Route, RouteDto> postProcess) {
        return routes.stream().map(r -> toDto(r, postProcess)).toList();
    }

    @Override
    public List<Route> fromDto(List<RouteDto> dtos) {
        return dtos.stream().map(this::fromDto).toList();
    }
}
