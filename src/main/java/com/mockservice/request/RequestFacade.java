package com.mockservice.request;

import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;
import java.util.Optional;

public interface RequestFacade {
    RequestMethod getRequestMethod();
    String getEndpoint();
    Optional<String> getAlt();
    Map<String, String> getVariables(Optional<Map<String, String>> baseVariables);
    String getBody();
}
