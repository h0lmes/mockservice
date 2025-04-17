package com.mockachu.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class TopicPartitionData {
    private final Map<Long, KafkaRecord> records = new HashMap<>();
    private final AtomicLong producerOffset = new AtomicLong(0);
    private final AtomicLong consumerOffset = new AtomicLong(0);

    public TopicPartitionData() {
        // default
    }

    public Long put(String topic, Integer partition, Long timestamp, String key, String value, Map<String, String> headers) {
        var rec = new KafkaRecord(producerOffset.getAndIncrement(), topic, partition, timestamp, key, value, headers);
        records.put(rec.offset(), rec);
        return rec.offset();
    }

    public synchronized Long seek(Long offset) {
        if (offset > -1L) consumerOffset.set(offset);
        return consumerOffset.get();
    }

    public synchronized List<KafkaRecord> get() {
        List<KafkaRecord> list = null;
        var rec = records.get(consumerOffset.get());
        while (rec != null) {
            if (list == null) {
                list = new ArrayList<>();
            }

            list.add(rec);
            rec = records.get(consumerOffset.incrementAndGet());
        }
        return list;
    }

    public Long getProducerOffset() {
        return producerOffset.get();
    }

    public Long getConsumerOffset() {
        return consumerOffset.get();
    }

    public List<KafkaRecord> getRecords(long offset, long limit) {
        List<KafkaRecord> result = new ArrayList<>();
        for (long i = 0; i < limit; i++) {
            var rec = records.get(offset + i);
            if (rec != null) result.add(rec);
        }
        return result;
    }
}
