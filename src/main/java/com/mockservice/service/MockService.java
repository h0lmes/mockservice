package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.request.RequestFacade;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ThreadLocalRandom;

public interface MockService {
    void cacheRemove(Route route);
    ResponseEntity<String> mock(RequestFacade request);

    default void delay() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(10, 3000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
