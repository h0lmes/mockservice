package com.mockservice.service;

import com.mockservice.response.MockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class RequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceImpl.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    @Override
    public void schedule(MockResponse response) {
        CompletableFuture.runAsync(
                () -> sendRequest(response),
                CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS)
        );
    }

    private void sendRequest(MockResponse mockResponse) {
        try {
            String url = mockResponse.getRequestUrl();

            log.info("Callback request: {}, {}, {}, {}",
                    mockResponse.getRequestMethod(),
                    url,
                    mockResponse.getRequestBody(),
                    mockResponse.getRequestHeaders());

            String response = WebClient.create()
                    .method(mockResponse.getRequestMethod())
                    .uri(url)
                    .bodyValue(mockResponse.getRequestBody())
                    .headers(c -> c.putAll(mockResponse.getRequestHeaders()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(REQUEST_TIMEOUT);
            log.info("Callback request response: {}", response);
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
