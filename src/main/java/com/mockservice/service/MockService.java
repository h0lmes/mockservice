package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.request.RequestFacade;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ThreadLocalRandom;

public interface MockService {
    void cacheRemove(Route route);
    ResponseEntity<String> mock(RequestFacade request);
}
