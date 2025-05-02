package com.mockachu.service;

import com.mockachu.domain.Route;
import com.mockachu.web.mock.MockRequestFacade;
import org.springframework.http.ResponseEntity;

public interface MockService {
    void cacheRemove(Route route);
    ResponseEntity<String> mock(MockRequestFacade request);
}
