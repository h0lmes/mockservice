package com.mockservice.request;

import com.mockservice.template.MockVariables;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;

public interface RequestFacade {
    RequestMethod getRequestMethod();
    String getEndpoint();
    Optional<String> getAlt();
    MockVariables getVariables(Optional<MockVariables> baseVariables);
    String getBody();
}
