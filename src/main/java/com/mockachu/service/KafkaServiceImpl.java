package com.mockachu.service;

import com.mockachu.domain.KafkaTopic;
import com.mockachu.kafka.*;
import com.mockachu.mapper.KafkaTopicMapper;
import com.mockachu.model.KafkaTopicDto;
import com.mockachu.model.KafkaTopicRecordsDto;
import com.mockachu.repository.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KafkaServiceImpl implements KafkaService {
    private static final Logger log = LoggerFactory.getLogger(KafkaServiceImpl.class);

    private final Map<TopicPartition, TopicPartitionData> map = new ConcurrentHashMap<>();
    private final ConfigRepository repository;
    private final KafkaTopicMapper mapper;

    public KafkaServiceImpl(ConfigRepository repository, KafkaTopicMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
        createAllTopicPartitionData();
    }

    private void createAllTopicPartitionData() {
        repository.findAllKafkaTopics().forEach(this::createTopicPartitionDataIfNotExists);
    }

    private void createTopicPartitionDataIfNotExists(KafkaTopic topic) {
        map.computeIfAbsent(new TopicPartition(topic.getTopic(), topic.getPartition()), e -> new TopicPartitionData());
    }

    private void deleteTopicPartitionData(KafkaTopic topic) {
        map.remove(new TopicPartition(topic.getTopic(), topic.getPartition()));
    }

    @Override
    public void produce(List<MockachuKafkaProducerRequest> requests) {
        requests.forEach(req -> {
            var tp = new TopicPartition(req.topic(), req.partition());
            var data = map.computeIfAbsent(tp, e -> new TopicPartitionData());
            var offset = data.put(req.topic(), req.partition(), req.timestamp(), req.key(), req.value(), req.headers());
            log.info("produced in {} offset {}", tp, offset);
        });
    }

    @Override
    public List<KafkaRecord> consume(List<MockachuKafkaConsumerRequest> requests) {
        List<KafkaRecord> list = new ArrayList<>();
        if (requests == null || requests.isEmpty()) return list;

        long startMillis = System.currentTimeMillis();
        long duration = requests.get(0).duration();

        while (list.isEmpty() && System.currentTimeMillis() - startMillis <= duration) {
            requests.forEach(request -> {
                var tp = new TopicPartition(request.topic(), request.partition());
                var data = map.computeIfAbsent(tp, e -> new TopicPartitionData());

                data.seek(request.seek());

                var kafkaRecords = data.get();
                if (kafkaRecords != null && !kafkaRecords.isEmpty()) {
                    list.addAll(kafkaRecords);

                    log.info("consumed from {} offsets from {} to {}",
                            tp,
                            kafkaRecords.get(0).offset(),
                            kafkaRecords.get(kafkaRecords.size() - 1).offset());
                }
            });

            if (list.isEmpty() && duration > 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return list;
    }

    @Override
    public List<KafkaTopicDto> list() {
        return map.entrySet().stream()
                .map(e -> {
                    var dto = repository.findKafkaTopic(
                            e.getKey().topic(), e.getKey().partition())
                            .map(mapper::toDto)
                            .orElse(new KafkaTopicDto()
                                    .setGroup("__volatile__")
                                    .setTopic(e.getKey().topic())
                                    .setPartition(e.getKey().partition())
                            );
                    dto.setProducerOffset(e.getValue().getProducerOffset());
                    dto.setConsumerOffset(e.getValue().getConsumerOffset());
                    return dto;
                })
                .toList();
    }

    @Override
    public synchronized void put(KafkaTopicDto reference, KafkaTopicDto topic) throws IOException {
        deleteTopicPartitionData(mapper.fromDto(reference));

        repository.putKafkaTopic(mapper.fromDto(reference), mapper.fromDto(topic));
        repository.findKafkaTopic(topic.getTopic(), topic.getPartition())
                .ifPresent(this::createTopicPartitionDataIfNotExists);
    }

    @Override
    public synchronized void putAll(List<KafkaTopicDto> list, boolean overwrite) throws IOException {
        repository.putKafkaTopics(mapper.fromDto(list), overwrite);
        mapper.fromDto(list).forEach(this::createTopicPartitionDataIfNotExists);
    }

    @Override
    public synchronized void deleteAll(List<KafkaTopicDto> list) throws IOException {
        repository.deleteKafkaTopics(mapper.fromDto(list));
        mapper.fromDto(list).forEach(this::deleteTopicPartitionData);
    }

    @Override
    public KafkaTopicRecordsDto listRecords(String topic, Integer partition,
                                            Long offset, Long limit) {
        var tp = new TopicPartition(topic, partition);
        var data = map.computeIfAbsent(tp, e -> new TopicPartitionData());

        return new KafkaTopicRecordsDto(
                data.getRecords(offset, limit),
                data.getProducerOffset(), offset, limit);
    }
}
