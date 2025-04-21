package com.mockachu.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MockachuKafkaConsumerRequest(long duration,
                                           String topic,
                                           int partition,
                                           Long seek) {

    public MockachuKafkaConsumerRequest(String topic,
                                        int partition,
                                        Long seek) {
        this(0, topic, partition, seek);
    }

    public MockachuKafkaConsumerRequest(long duration,
                                        String topic,
                                        int partition,
                                        Long seek) {
        this.duration = duration;
        this.topic = topic;
        this.partition = partition;
        this.seek = seek == null ? -1L : seek; // default is -1
    }
}
