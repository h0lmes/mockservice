package com.mockachu.mapper;

import com.mockachu.domain.OutboundRequest;
import com.mockachu.model.OutboundRequestDto;

import java.util.List;

public interface OutboundRequestMapper {
    OutboundRequestDto toDto(OutboundRequest request);

    OutboundRequest fromDto(OutboundRequestDto dto);

    List<OutboundRequestDto> toDto(List<OutboundRequest> requests);

    List<OutboundRequest> fromDto(List<OutboundRequestDto> dtos);
}
