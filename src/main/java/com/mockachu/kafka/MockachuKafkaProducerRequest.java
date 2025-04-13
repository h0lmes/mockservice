package com.mockachu.kafka;

import java.util.Map;

public record MockachuKafkaProducerRequest(String topic,
                                           Integer partition,
                                           Long timestamp,
                                           String key,
                                           String value,
                                           Map<String, String> headers) {
}
