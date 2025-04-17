package com.mockachu.exception;

import com.mockachu.domain.KafkaTopic;

public class KafkaTopicAlreadyExistsException extends RuntimeException {

    private final transient KafkaTopic o;

    public KafkaTopicAlreadyExistsException(KafkaTopic o) {
        this.o = o;
    }

    @Override
    public String getMessage() {
        return "Kafka topic already exists: " + o;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
