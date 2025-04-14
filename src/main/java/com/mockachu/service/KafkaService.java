package com.mockachu.service;

import com.mockachu.kafka.KafkaRecord;
import com.mockachu.kafka.MockachuKafkaConsumerRequest;
import com.mockachu.kafka.MockachuKafkaProducerRequest;

import java.util.List;

public interface KafkaService {
    void produce(List<MockachuKafkaProducerRequest> requests);
    List<KafkaRecord> consume(List<MockachuKafkaConsumerRequest> requests);
}
