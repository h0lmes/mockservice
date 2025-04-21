package com.mockachu.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MockachuKafkaProducerRequest(String topic,
                                           int partition,
                                           Long timestamp,
                                           String key,
                                           String value,
                                           Map<String, String> headers) {
}
