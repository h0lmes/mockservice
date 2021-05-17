package com.mockservice.request;

import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

public interface RequestFacade {
    String getMethod();
    RequestMethod getRequestMethod();
    String getEndpoint();
    String getPath(String group);
    Map<String, String> getVariables(String group, @Nullable Map<String, String> variables);
}
