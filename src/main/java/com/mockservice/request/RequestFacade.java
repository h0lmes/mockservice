package com.mockservice.request;

import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface RequestFacade {
    String getGroup();
    String getMethod();
    HttpServletRequest getRequest();
    String getPath();
    Map<String, String> getVariables(@Nullable Map<String, String> variables);
}
