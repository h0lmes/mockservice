package com.mockservice.service;

import com.mockservice.domain.OutboundRequest;
import com.mockservice.model.OutboundRequestDto;
import com.mockservice.template.MockVariables;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface RequestService {
    void schedule(String requestIds, MockVariables variables);
    Optional<String> executeRequest(String requestId, MockVariables variables, boolean allowTrigger);
    Optional<OutboundRequest> getEnabledRequest(String requestId);
    List<OutboundRequestDto> getRequests();
    void putRequest(OutboundRequestDto existing, OutboundRequestDto request) throws IOException;
    void putRequests(List<OutboundRequestDto> dto, boolean overwrite) throws IOException;
    void deleteRequests(List<OutboundRequestDto> dto) throws IOException;
}
