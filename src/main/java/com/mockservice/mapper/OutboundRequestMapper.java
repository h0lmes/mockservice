package com.mockservice.mapper;

import com.mockservice.domain.OutboundRequest;
import com.mockservice.model.OutboundRequestDto;

import java.util.List;

public interface OutboundRequestMapper {
    OutboundRequestDto toDto(OutboundRequest request);

    OutboundRequest fromDto(OutboundRequestDto dto);

    List<OutboundRequestDto> toDto(List<OutboundRequest> requests);

    List<OutboundRequest> fromDto(List<OutboundRequestDto> dtos);
}
