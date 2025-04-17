package com.mockachu.model;

import com.mockachu.kafka.KafkaRecord;

import java.util.List;

public class KafkaTopicRecordsDto {
    private final List<KafkaRecord> records;
    private final Long total;
    private final Long offset;
    private final Long limit;

    public KafkaTopicRecordsDto(List<KafkaRecord> records,
                                Long total,
                                Long offset,
                                Long limit) {
        this.records = records;
        this.total = total;
        this.offset = offset;
        this.limit = limit;
    }

    public List<KafkaRecord> getRecords() {
        return records;
    }

    public Long getTotal() {
        return total;
    }

    public Long getOffset() {
        return offset;
    }

    public Long getLimit() {
        return limit;
    }
}
