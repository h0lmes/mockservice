package com.kafkatest.mockachu;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class MockachuKafkaSenderWebClientAdapter implements MockachuKafkaSender {
    private final WebClient client;

    public MockachuKafkaSenderWebClientAdapter(String baseUri, Duration timeout) {
        var httpClient = HttpClient.create().responseTimeout(timeout);
        var clientHttpConnector = new ReactorClientHttpConnector(httpClient);
        this.client = WebClient.builder().clientConnector(clientHttpConnector).baseUrl(baseUri).build();
    }

    public CompletableFuture<String> sendAsync(String message) {
        var future = new CompletableFuture<String>();
        client.post().bodyValue(message)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(future::complete, future::completeExceptionally);
        return future;
    }

    public String sendSync(String message) {
        return client.post().bodyValue(message)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
