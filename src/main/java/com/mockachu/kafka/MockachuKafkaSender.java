package com.mockachu.kafka;

import java.util.concurrent.CompletableFuture;

public interface MockachuKafkaSender {
    CompletableFuture<String> send(String message);
}
