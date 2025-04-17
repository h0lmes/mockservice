package com.mockachu.kafka;

import java.util.Objects;

public class TopicPartition {
    private final String topic;
    private final int partition;
    private int hash = 0;

    public TopicPartition(String topic, int partition) {
        this.partition = partition;
        this.topic = topic;
    }

    public int partition() {
        return this.partition;
    }

    public String topic() {
        return this.topic;
    }

    public int hashCode() {
        if (hash != 0) {
            return hash;
        } else {
            int result = 63 + partition;
            result = 63 * result + Objects.hashCode(topic);
            hash = result;
            return result;
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            TopicPartition other = (TopicPartition)obj;
            return partition == other.partition && Objects.equals(topic, other.topic);
        }
    }

    public String toString() {
        return topic + "-" + partition;
    }
}
