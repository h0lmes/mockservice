package com.mockachu.config;

import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.CompletableFuture;

public interface MockachuKafkaSender {
    CompletableFuture<RecordMetadata> send(String uri, String message, String topic, int partition);
}
