package com.mockachu.model;

public class KafkaTopicDto {

    private String group = "";
    private String topic = "";
    private int partition;
    private String initialData = "";
    private boolean persistent = false;

    private long producerOffset;
    private long consumerOffset;

    public KafkaTopicDto() {
        // default
    }

    public String getGroup() {
        return group;
    }

    public KafkaTopicDto setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public KafkaTopicDto setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getPartition() {
        return partition;
    }

    public KafkaTopicDto setPartition(int partition) {
        this.partition = partition;
        return this;
    }

    public String getInitialData() {
        return initialData;
    }

    public KafkaTopicDto setInitialData(String initialData) {
        this.initialData = initialData == null ? "" : initialData;
        return this;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public KafkaTopicDto setPersistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    public long getProducerOffset() {
        return producerOffset;
    }

    public void setProducerOffset(long producerOffset) {
        this.producerOffset = producerOffset;
    }

    public long getConsumerOffset() {
        return consumerOffset;
    }

    public void setConsumerOffset(long consumerOffset) {
        this.consumerOffset = consumerOffset;
    }

    @Override
    public String toString() {
        return topic + "-" + partition;
    }
}
