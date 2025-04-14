package com.mockachu.service;

import com.mockachu.kafka.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KafkaServiceImpl implements KafkaService {
    private static final Logger log = LoggerFactory.getLogger(KafkaServiceImpl.class);

    private final Map<TopicPartition, TopicPartitionData> map = new ConcurrentHashMap<>();

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
        return list;
    }
}
