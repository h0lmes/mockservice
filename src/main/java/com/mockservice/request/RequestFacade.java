package com.mockservice.request;

import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;
import java.util.Optional;

public interface RequestFacade {
    RequestMethod getRequestMethod();
    String getEndpoint();
    Optional<String> getSuffix();
    Map<String, String> getVariables(@Nullable Map<String, String> variables);
}
