package com.mockachu.mapper;

import com.mockachu.domain.Route;
import com.mockachu.model.RouteDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteMapperImpl implements RouteMapper {

    @Override
    public RouteDto toDto(Route route) {
        return new RouteDto()
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
    public List<RouteDto> toDto(List<Route> routes) {
        return routes.stream().map(this::toDto).toList();
    }

    @Override
    public List<Route> fromDto(List<RouteDto> list) {
        return list.stream().map(this::fromDto).toList();
    }
}
