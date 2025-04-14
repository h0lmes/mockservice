package com.mockachu.kafka;

import java.util.Map;

public record KafkaRecord(Long offset,
                          String topic,
                          Integer partition,
                          Long timestamp,
                          String key,
                          String value,
                          Map<String, String> headers) {
}
