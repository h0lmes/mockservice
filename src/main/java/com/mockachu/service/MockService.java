package com.mockachu.service;

import com.mockachu.domain.Route;
import com.mockachu.request.RequestFacade;
import org.springframework.http.ResponseEntity;

public interface MockService {
    void cacheRemove(Route route);
    ResponseEntity<String> mock(RequestFacade request);
}
