package com.mockachu.domain;

import java.util.Objects;

public class KafkaTopic implements Comparable<KafkaTopic> {

    private String group = "";
    private String topic = "";
    private int partition = 0;

    public KafkaTopic() {
        // default
    }

    public String getGroup() {
        return group;
    }

    public KafkaTopic setGroup(String group) {
        this.group = group == null ? "" : group;
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public KafkaTopic setTopic(String topic) {
        this.topic = topic == null ? "" : topic;
        return this;
    }

    public int getPartition() {
        return partition;
    }

    public KafkaTopic setPartition(int partition) {
        this.partition = partition;
        return this;
    }

    public KafkaTopic assignFrom(KafkaTopic source) {
        setGroup(source.getGroup());
        setTopic(source.getTopic());
        setPartition(source.getPartition());
        return this;
    }

    @Override
    public int hashCode() {
        int result = 63 + partition;
        return 63 * result + Objects.hashCode(topic);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KafkaTopic other)) return false;
        return topic.equals(other.getTopic())
                && partition == other.getPartition();
    }

    @Override
    public String toString() {
        return topic + "-" + partition;
    }

    @Override
    public int compareTo(KafkaTopic o) {
        int c;
        c = this.group.compareTo(o.getGroup());
        if (c != 0) return c;
        c = this.topic.compareTo(o.getTopic());
        if (c != 0) return c;
        c = Integer.compare(partition, o.getPartition());
        return c;
    }
}
