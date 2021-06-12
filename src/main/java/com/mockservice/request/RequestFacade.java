package com.mockservice.request;

import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;
import java.util.Optional;

public interface RequestFacade {
    String getBasePath();
    RequestMethod getRequestMethod();
    String getEndpoint();
    Optional<String> getAlt();
    Map<String, String> getVariables();
}
