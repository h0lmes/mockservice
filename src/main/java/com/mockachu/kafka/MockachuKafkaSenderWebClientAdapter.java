package com.mockachu.kafka;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.CompletableFuture;

public class MockachuKafkaSenderWebClientAdapter implements MockachuKafkaSender {
    private final WebClient client;

    public MockachuKafkaSenderWebClientAdapter(String baseUri) {
        var httpClient = HttpClient.create();
        var clientHttpConnector = new ReactorClientHttpConnector(httpClient);
        this.client = WebClient.builder().clientConnector(clientHttpConnector).baseUrl(baseUri).build();
    }

    public CompletableFuture<String> send(String message) {
        var future = new CompletableFuture<String>();
        client.post().uri("").bodyValue(message)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(future::complete, future::completeExceptionally);
        return future;
    }
}
