package com.mockservice.service;

import com.mockservice.response.MockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class RequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceImpl.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private final LinkedBlockingQueue<MockResponse> requestQueue = new LinkedBlockingQueue<>();

    @Override
    public void schedule(MockResponse response) {
        try {
            requestQueue.put(response);
        } catch (InterruptedException e) {
            log.warn("", e);
            Thread.currentThread().interrupt();
        }
    }

    @Scheduled(initialDelayString = "${application.request-service.initial-delay}", fixedDelayString = "${application.request-service.fixed-delay}")
    public void scheduleFixedDelay() {
        try {
            MockResponse response = requestQueue.poll(10, TimeUnit.MILLISECONDS);
            while (response != null) {
                sendRequest(response);
                response = requestQueue.poll(10, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            log.warn("", e);
            Thread.currentThread().interrupt();
        }
    }

    private void sendRequest(MockResponse mockResponse) {
        try {
            String url = mockResponse.getRequestUrl();
            InetSocketAddress host = mockResponse.getRequestHeaders().getHost();
            if (host != null && url.startsWith("/")) {
                url = host.toString() + url;
            }

            log.info("sendRequest():\nmethod = {}\nurl = {}\nbody = {}\nheaders = {}",
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
            log.info(response);
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
