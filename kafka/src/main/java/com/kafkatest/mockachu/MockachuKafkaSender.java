package com.kafkatest.mockachu;

import java.util.concurrent.CompletableFuture;

public interface MockachuKafkaSender {
    CompletableFuture<String> sendAsync(String message);
    String sendSync(String message);
}
