package com.mockservice.request;

import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

public interface RequestFacade {
    String getMethod();
    RequestMethod getRequestMethod();
    String getEndpoint();
    String getSuffix();
    Map<String, String> getVariables(@Nullable Map<String, String> variables);
}
