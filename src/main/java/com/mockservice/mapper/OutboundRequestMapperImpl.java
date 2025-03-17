package com.mockservice.mapper;

import com.mockservice.domain.OutboundRequest;
import com.mockservice.model.OutboundRequestDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboundRequestMapperImpl implements OutboundRequestMapper {

    @Override
    public OutboundRequestDto toDto(OutboundRequest request) {
        return new OutboundRequestDto()
                .setId(request.getId())
                .setGroup(request.getGroup())
                .setType(request.getType())
                .setMethod(request.getMethod())
                .setPath(request.getPath())
                .setBody(request.getBody())
                .setDisabled(request.isDisabled())
                .setTriggerRequest(request.isTriggerRequest())
                .setTriggerRequestIds(request.getTriggerRequestIds());
    }

    @Override
    public OutboundRequest fromDto(OutboundRequestDto dto) {
        return new OutboundRequest()
                .setId(dto.getId())
                .setGroup(dto.getGroup())
                .setType(dto.getType())
                .setMethod(dto.getMethod())
                .setPath(dto.getPath())
                .setBody(dto.getBody())
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
