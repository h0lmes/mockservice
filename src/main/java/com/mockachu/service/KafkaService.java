package com.mockachu.service;

import com.mockachu.kafka.KafkaRecord;
import com.mockachu.kafka.MockachuKafkaConsumerRequest;
import com.mockachu.kafka.MockachuKafkaProducerRequest;
import com.mockachu.model.KafkaTopicDto;
import com.mockachu.model.KafkaTopicRecordsDto;

import java.io.IOException;
import java.util.List;

public interface KafkaService {
    void produce(List<MockachuKafkaProducerRequest> requests);
    List<KafkaRecord> consume(List<MockachuKafkaConsumerRequest> requests);
    List<KafkaTopicDto> list();
    void put(KafkaTopicDto reference, KafkaTopicDto topic) throws IOException;
    void putAll(List<KafkaTopicDto> list, boolean overwrite) throws IOException;
    void deleteAll(List<KafkaTopicDto> list) throws IOException;
    KafkaTopicRecordsDto listRecords(String topic, Integer partition, Long offset, Long limit);
}
