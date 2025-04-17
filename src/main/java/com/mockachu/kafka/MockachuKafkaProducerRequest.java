package com.mockachu.kafka;

import java.util.Map;

public record MockachuKafkaProducerRequest(String topic,
                                           int partition,
                                           long timestamp,
                                           String key,
                                           String value,
                                           Map<String, String> headers) {
}
