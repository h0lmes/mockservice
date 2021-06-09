package com.mockservice.service;

import com.mockservice.domain.Route;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public interface MockService {
    int MIN_DELAY = 10;
    int MAX_DELAY = 3000;

    void cacheRemove(Route route);
    ResponseEntity<String> mock(Map<String, String> variables);
    String mockError(Throwable t);

    default void delay() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(MIN_DELAY, MAX_DELAY));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
