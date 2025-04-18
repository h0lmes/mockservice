package com.mockachu.request;

import com.mockachu.template.MockVariables;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;

public interface RequestFacade {
    RequestMethod getMethod();
    String getEndpoint();
    Optional<String> getAlt();
    MockVariables getVariables(@Nullable MockVariables baseVariables);
    String getBody();
}
