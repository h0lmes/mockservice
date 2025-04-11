package com.mockachu.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.record.TimestampType;
import org.apache.kafka.common.serialization.Deserializer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MockachuKafkaConsumer<K, V> implements Consumer<K, V> {
    private static record LocalAssignment () {}

    private final String clientId;
    private final ConsumerConfig config;
    private final Deserializer<K> keyDeserializer;
    private final Deserializer<V> valueDeserializer;
    private final long retryBackoffMs;
    private final int defaultApiTimeoutMs;
    private final boolean autoCommitEnabled;
    private Set<String> subscriptions;
    private Map<TopicPartition, LocalAssignment> assignments;
    private final AtomicBoolean wakeup = new AtomicBoolean(false);

    public MockachuKafkaConsumer(ConsumerConfig config,
                                 Deserializer<K> keyDeserializer,
                                 Deserializer<V> valueDeserializer) {
        this.clientId = config.getString("client.id");
        this.config = config;
        this.keyDeserializer = keyDeserializer;
        this.valueDeserializer = valueDeserializer;
        this.defaultApiTimeoutMs = config.getInt("default.api.timeout.ms");
        this.retryBackoffMs = config.getLong("retry.backoff.ms");
        this.autoCommitEnabled = config.getBoolean("enable.auto.commit");
    }

    @Override
    public Set<TopicPartition> assignment() {
        return subscriptions.stream()
                .map(topic -> new TopicPartition(topic, 0))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> subscription() {
        return Collections.unmodifiableSet(subscriptions);
    }

    @Override
    public void subscribe(Collection<String> collection) {
        subscribe(collection, null);
    }

    @Override
    public void subscribe(Collection<String> collection, ConsumerRebalanceListener consumerRebalanceListener) {
        subscriptions = new HashSet<>(collection);
        updateAssignments();
    }

    @Override
    public void assign(Collection<TopicPartition> collection) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public void subscribe(Pattern pattern, ConsumerRebalanceListener consumerRebalanceListener) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public void subscribe(Pattern pattern) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public void unsubscribe() {
        subscriptions = Collections.emptySet();
        updateAssignments();
    }

    private void updateAssignments() {
        // TODO
    }

    @Override
    public ConsumerRecords<K, V> poll(long l) {
        if (wakeup.get()) throw new WakeupException();

        TimestampType timestampType = TimestampType.CREATE_TIME;
        // TODO
        // record is:
//        String topic; - have
//        int partition; - have
//        long offset;
//        long timestamp;
//        TimestampType timestampType;
//        int serializedKeySize; - calc on client
//        int serializedValueSize; - calc on client
//        Headers headers;
//        K key;
//        V value;
//        private final Optional<Integer> leaderEpoch; always = 0
        return null;
    }

    // server data struct: offset, time cli, time srv, key, value

    // no headers support for now

    // client connect -> get assignments ->
    // other consumers of this topic get new assignments on next poll

    // close -> client disconnect ->
    // other consumers of this topic get new assignments

    // seek

    @Override
    public ConsumerRecords<K, V> poll(Duration duration) {
        return poll(duration.toMillis());
    }

    @Override
    public void commitSync() {
        commitSync(Duration.ofMillis(defaultApiTimeoutMs));
    }

    @Override
    public void commitSync(Duration duration) {
        // TODO ?
    }

    @Override
    public void commitSync(Map<TopicPartition, OffsetAndMetadata> map) {
        // TODO ?
    }

    @Override
    public void commitSync(Map<TopicPartition, OffsetAndMetadata> map, Duration duration) {
        // TODO ?
    }

    @Override
    public void commitAsync() {
        commitAsync(null);
    }

    @Override
    public void commitAsync(OffsetCommitCallback offsetCommitCallback) {
        // TODO ?
    }

    @Override
    public void commitAsync(Map<TopicPartition, OffsetAndMetadata> map, OffsetCommitCallback offsetCommitCallback) {
        // do nothing
    }

    @Override
    public void seek(TopicPartition topicPartition, long l) {
        // TODO ?
    }

    @Override
    public void seek(TopicPartition topicPartition, OffsetAndMetadata offsetAndMetadata) {
        // TODO ?
    }

    @Override
    public void seekToBeginning(Collection<TopicPartition> collection) {
        // TODO ?
    }

    @Override
    public void seekToEnd(Collection<TopicPartition> collection) {
        // TODO ?
    }

    @Override
    public long position(TopicPartition topicPartition) {
        // TODO
        return 0;
    }

    @Override
    public long position(TopicPartition topicPartition, Duration duration) {
        // TODO
        return 0;
    }

    @Override
    public OffsetAndMetadata committed(TopicPartition topicPartition) {
        // TODO
        return null;
    }

    @Override
    public OffsetAndMetadata committed(TopicPartition topicPartition, Duration duration) {
        // TODO
        return null;
    }

    @Override
    public Map<TopicPartition, OffsetAndMetadata> committed(Set<TopicPartition> set) {
        Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
        set.forEach(tp -> map.put(tp, committed(tp)));
        return map;
    }

    @Override
    public Map<TopicPartition, OffsetAndMetadata> committed(Set<TopicPartition> set, Duration duration) {
        Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
        set.forEach(tp -> map.put(tp, committed(tp, duration)));
        return map;
    }

    @Override
    public Uuid clientInstanceId(Duration duration) {
        // TODO ?
        return null;
    }

    @Override
    public Map<MetricName, ? extends Metric> metrics() {
        return null;
    }

    @Override
    public List<PartitionInfo> partitionsFor(String s) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public List<PartitionInfo> partitionsFor(String s, Duration duration) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<String, List<PartitionInfo>> listTopics() {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<String, List<PartitionInfo>> listTopics(Duration duration) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Set<TopicPartition> paused() {
        return Collections.emptySet();
    }

    @Override
    public void pause(Collection<TopicPartition> collection) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public void resume(Collection<TopicPartition> collection) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> map) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> map, Duration duration) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<TopicPartition, Long> beginningOffsets(Collection<TopicPartition> collection) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<TopicPartition, Long> beginningOffsets(Collection<TopicPartition> collection, Duration duration) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<TopicPartition, Long> endOffsets(Collection<TopicPartition> collection) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<TopicPartition, Long> endOffsets(Collection<TopicPartition> collection, Duration duration) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public OptionalLong currentLag(TopicPartition topicPartition) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public ConsumerGroupMetadata groupMetadata() {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public void enforceRebalance() {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public void enforceRebalance(String s) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public void close() {
        // TODO ?
    }

    @Override
    public void close(Duration duration) {
        // TODO ?
    }

    @Override
    public void wakeup() {
        wakeup.set(true);
    }
}
