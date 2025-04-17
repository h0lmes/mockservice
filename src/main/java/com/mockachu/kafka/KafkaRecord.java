package com.mockachu.kafka;

import java.util.Map;

public record KafkaRecord(long offset,
                          String topic,
                          int partition,
                          long timestamp,
                          String key,
                          String value,
                          Map<String, String> headers) {
}
