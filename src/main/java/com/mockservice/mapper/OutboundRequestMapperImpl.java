package com.mockservice.mapper;

import com.mockservice.domain.OutboundRequest;
import com.mockservice.model.OutboundRequestDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboundRequestMapperImpl implements OutboundRequestMapper {

    @Override
    public OutboundRequestDto toDto(OutboundRequest entity) {
        return new OutboundRequestDto()
                .setId(entity.getId())
                .setGroup(entity.getGroup())
                .setType(entity.getType())
                .setMethod(entity.getMethod())
                .setPath(entity.getPath())
                .setHeaders(entity.getHeaders())
                .setBody(entity.getBody())
                .setResponseToVars(entity.isResponseToVars())
                .setDisabled(entity.isDisabled())
                .setTriggerRequest(entity.isTriggerRequest())
                .setTriggerRequestIds(entity.getTriggerRequestIds());
    }

    @Override
    public OutboundRequest fromDto(OutboundRequestDto dto) {
        return new OutboundRequest()
                .setId(dto.getId())
                .setGroup(dto.getGroup())
                .setType(dto.getType())
                .setMethod(dto.getMethod())
                .setPath(dto.getPath())
                .setHeaders(dto.getHeaders())
                .setBody(dto.getBody())
                .setResponseToVars(dto.isResponseToVars())
                .setDisabled(dto.isDisabled())
                .setTriggerRequest(dto.isTriggerRequest())
                .setTriggerRequestIds(dto.getTriggerRequestIds());
    }

    @Override
    public List<OutboundRequestDto> toDto(List<OutboundRequest> requests) {
        return requests.stream().map(this::toDto).toList();
    }

    @Override
    public List<OutboundRequest> fromDto(List<OutboundRequestDto> dtos) {
        return dtos.stream().map(this::fromDto).toList();
    }
}
