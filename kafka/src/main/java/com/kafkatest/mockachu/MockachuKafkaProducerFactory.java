package com.kafkatest.mockachu;

import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.*;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.log.LogAccessor;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class MockachuKafkaProducerFactory<K, V> extends KafkaResourceFactory implements ProducerFactory<K, V>, ApplicationContextAware, BeanNameAware, ApplicationListener<ContextStoppedEvent>, DisposableBean, SmartLifecycle {
    private static final LogAccessor LOGGER = new LogAccessor(LogFactory.getLog(MockachuKafkaProducerFactory.class));
    private final ReentrantLock globalLock;
    private final Map<String, Object> configs;
    private final Map<String, BlockingQueue<MockachuKafkaProducerFactory.CloseSafeProducer<K, V>>> cache;
    private final Map<Thread, MockachuKafkaProducerFactory.CloseSafeProducer<K, V>> threadBoundProducers;
    private final AtomicInteger epoch;
    private final AtomicInteger clientIdCounter;
    private final List<Listener<K, V>> listeners;
    private final List<ProducerPostProcessor<K, V>> postProcessors;
    private final AtomicBoolean running;
    private TransactionIdSuffixStrategy transactionIdSuffixStrategy;
    private Supplier<Serializer<K>> keySerializerSupplier;
    private Supplier<Serializer<V>> valueSerializerSupplier;
    private Supplier<Serializer<K>> rawKeySerializerSupplier;
    private Supplier<Serializer<V>> rawValueSerializerSupplier;
    private Duration physicalCloseTimeout;
    private ApplicationContext applicationContext;
    private String beanName;
    private boolean producerPerThread;
    private long maxAge;
    private boolean configureSerializers;
    private volatile String transactionIdPrefix;
    private volatile String clientIdPrefix;
    private volatile MockachuKafkaProducerFactory.CloseSafeProducer<K, V> producer;

    private final MockachuKafkaSender sender;

    public MockachuKafkaProducerFactory(Map<String, Object> configs,
                                        MockachuKafkaSender sender) {
        this(configs, () -> null, () -> null, sender);
    }

    public MockachuKafkaProducerFactory(Map<String, Object> configs,
                                        @Nullable Serializer<K> keySerializer,
                                        @Nullable Serializer<V> valueSerializer,
                                        MockachuKafkaSender sender) {
        this(configs, () -> keySerializer, () -> valueSerializer, true, sender);
    }

    public MockachuKafkaProducerFactory(Map<String, Object> configs,
                                        @Nullable Serializer<K> keySerializer,
                                        @Nullable Serializer<V> valueSerializer,
                                        boolean configureSerializers,
                                        MockachuKafkaSender sender) {
        this(configs, () -> keySerializer, () -> valueSerializer, configureSerializers, sender);
    }

    public MockachuKafkaProducerFactory(Map<String, Object> configs,
                                        @Nullable Supplier<Serializer<K>> keySerializerSupplier,
                                        @Nullable Supplier<Serializer<V>> valueSerializerSupplier,
                                        MockachuKafkaSender sender) {
        this(configs, keySerializerSupplier, valueSerializerSupplier, true, sender);
    }

    public MockachuKafkaProducerFactory(Map<String, Object> configs,
                                        @Nullable Supplier<Serializer<K>> keySerializerSupplier,
                                        @Nullable Supplier<Serializer<V>> valueSerializerSupplier,
                                        boolean configureSerializers,
                                        MockachuKafkaSender sender) {
        this.sender = sender;

        this.globalLock = new ReentrantLock();
        this.cache = new ConcurrentHashMap<>();
        this.threadBoundProducers = new ConcurrentHashMap<>();
        this.epoch = new AtomicInteger();
        this.clientIdCounter = new AtomicInteger();
        this.listeners = new ArrayList<>();
        this.postProcessors = new ArrayList<>();
        this.running = new AtomicBoolean();
        this.transactionIdSuffixStrategy = new DefaultTransactionIdSuffixStrategy(0);
        this.physicalCloseTimeout = DEFAULT_PHYSICAL_CLOSE_TIMEOUT;
        this.beanName = "not.managed.by.Spring";
        this.configureSerializers = true;
        this.configs = new ConcurrentHashMap<>(configs);
        this.configureSerializers = configureSerializers;
        this.keySerializerSupplier = this.keySerializerSupplier(keySerializerSupplier);
        this.valueSerializerSupplier = this.valueSerializerSupplier(valueSerializerSupplier);
        if (this.clientIdPrefix == null && configs.get("client.id") instanceof String cliIdPrefix) {
            this.clientIdPrefix = cliIdPrefix;
        }

        String txId = (String) this.configs.get("transactional.id");
        if (StringUtils.hasText(txId)) {
            this.setTransactionIdPrefix(txId);
            this.configs.remove("transactional.id");
        }
    }

    private Supplier<Serializer<K>> keySerializerSupplier(@Nullable Supplier<Serializer<K>> keySerializerSupplier) {
        this.rawKeySerializerSupplier = keySerializerSupplier;
        if (!this.configureSerializers) {
            return keySerializerSupplier;
        } else {
            return keySerializerSupplier == null ? () -> null : () -> {
                Serializer<K> serializer = keySerializerSupplier.get();
                if (serializer != null) {
                    serializer.configure(this.configs, true);
                }
                return serializer;
            };
        }
    }

    private Supplier<Serializer<V>> valueSerializerSupplier(@Nullable Supplier<Serializer<V>> valueSerializerSupplier) {
        this.rawValueSerializerSupplier = valueSerializerSupplier;
        if (!this.configureSerializers) {
            return valueSerializerSupplier;
        } else {
            return valueSerializerSupplier == null ? () -> null : () -> {
                Serializer<V> serializer = valueSerializerSupplier.get();
                if (serializer != null) {
                    serializer.configure(this.configs, false);
                }
                return serializer;
            };
        }
    }

    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setBeanName(@NonNull String name) {
        this.beanName = name;
    }

    public void setTransactionIdSuffixStrategy(TransactionIdSuffixStrategy transactionIdSuffixStrategy) {
        Assert.notNull(transactionIdSuffixStrategy, "'transactionIdSuffixStrategy' cannot be null");
        this.transactionIdSuffixStrategy = transactionIdSuffixStrategy;
    }

    public boolean isConfigureSerializers() {
        return this.configureSerializers;
    }

    public void setConfigureSerializers(boolean configureSerializers) {
        this.configureSerializers = configureSerializers;
    }

    @Override
    public Duration getPhysicalCloseTimeout() {
        return this.physicalCloseTimeout;
    }

    public void setPhysicalCloseTimeout(int physicalCloseTimeout) {
        this.physicalCloseTimeout = Duration.ofSeconds(physicalCloseTimeout);
    }

    @Override
    @Nullable
    public String getTransactionIdPrefix() {
        return this.transactionIdPrefix;
    }

    public final void setTransactionIdPrefix(String transactionIdPrefix) {
        Assert.notNull(transactionIdPrefix, "'transactionIdPrefix' cannot be null");
        this.transactionIdPrefix = transactionIdPrefix;
        this.enableIdempotentBehaviour();
    }

    @Override
    public boolean isProducerPerThread() {
        return this.producerPerThread;
    }

    public void setProducerPerThread(boolean producerPerThread) {
        this.producerPerThread = producerPerThread;
    }

    @Override
    @Nullable
    public Serializer<K> getKeySerializer() {
        return this.keySerializerSupplier.get();
    }

    public void setKeySerializer(@Nullable Serializer<K> keySerializer) {
        this.keySerializerSupplier = this.keySerializerSupplier(() -> keySerializer);
    }

    @Nullable
    public Serializer<V> getValueSerializer() {
        return this.valueSerializerSupplier.get();
    }

    public void setValueSerializer(@Nullable Serializer<V> valueSerializer) {
        this.valueSerializerSupplier = this.valueSerializerSupplier(() -> valueSerializer);
    }

    @Override
    public Supplier<Serializer<K>> getKeySerializerSupplier() {
        return this.rawKeySerializerSupplier;
    }

    public void setKeySerializerSupplier(Supplier<Serializer<K>> keySerializerSupplier) {
        this.keySerializerSupplier = this.keySerializerSupplier(keySerializerSupplier);
    }

    @Override
    public Supplier<Serializer<V>> getValueSerializerSupplier() {
        return this.rawValueSerializerSupplier;
    }

    public void setValueSerializerSupplier(Supplier<Serializer<V>> valueSerializerSupplier) {
        this.valueSerializerSupplier = this.valueSerializerSupplier(valueSerializerSupplier);
    }

    @Override
    public Map<String, Object> getConfigurationProperties() {
        Map<String, Object> configs2 = new HashMap<>(this.configs);
        this.checkBootstrap(configs2);
        return Collections.unmodifiableMap(configs2);
    }

    @Override
    public List<Listener<K, V>> getListeners() {
        return Collections.unmodifiableList(this.listeners);
    }

    @Override
    public List<ProducerPostProcessor<K, V>> getPostProcessors() {
        return Collections.unmodifiableList(this.postProcessors);
    }

    public void setMaxAge(Duration maxAge) {
        this.maxAge = maxAge.toMillis();
    }

    public void start() {
        this.running.set(true);
    }

    public void stop() {
        this.running.set(false);
        this.destroy();
    }

    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public int getPhase() {
        return -2147483648;
    }

    @Override
    public ProducerFactory<K, V> copyWithConfigurationOverride(@NonNull Map<String, Object> overrideProperties) {
        Map<String, Object> producerProperties = new HashMap<>(this.getConfigurationProperties());
        producerProperties.putAll(overrideProperties);
        producerProperties = this.ensureExistingTransactionIdPrefixInProperties(producerProperties);
        MockachuKafkaProducerFactory<K, V> newFactory = new MockachuKafkaProducerFactory<>(
                producerProperties,
                this.getKeySerializerSupplier(),
                this.getValueSerializerSupplier(),
                this.isConfigureSerializers(),
                sender);
        newFactory.setPhysicalCloseTimeout((int) this.getPhysicalCloseTimeout().getSeconds());
        newFactory.setProducerPerThread(this.isProducerPerThread());

        for (ProducerPostProcessor<K, V> templatePostProcessor : this.getPostProcessors()) {
            newFactory.addPostProcessor(templatePostProcessor);
        }

        for (Listener<K, V> templateListener : this.getListeners()) {
            newFactory.addListener(templateListener);
        }

        return newFactory;
    }

    private Map<String, Object> ensureExistingTransactionIdPrefixInProperties(Map<String, Object> producerProperties) {
        String txIdPrefix = this.getTransactionIdPrefix();
        if (StringUtils.hasText(txIdPrefix) && !producerProperties.containsKey("transactional.id")) {
            Map<String, Object> producerPropertiesWithTxnId = new HashMap<>(producerProperties);
            producerPropertiesWithTxnId.put("transactional.id", txIdPrefix);
            return producerPropertiesWithTxnId;
        } else {
            return producerProperties;
        }
    }

    @Override
    public void addListener(@NonNull Listener<K, V> listener) {
        Assert.notNull(listener, "'listener' cannot be null");
        this.listeners.add(listener);
    }

    @Override
    public void addListener(int index, @NonNull Listener<K, V> listener) {
        Assert.notNull(listener, "'listener' cannot be null");
        if (index >= this.listeners.size()) {
            this.listeners.add(listener);
        } else {
            this.listeners.add(index, listener);
        }

    }

    @Override
    public boolean removeListener(Listener<K, V> listener) {
        return this.listeners.remove(listener);
    }

    @Override
    public void addPostProcessor(@NonNull ProducerPostProcessor<K, V> postProcessor) {
        Assert.notNull(postProcessor, "'postProcessor' cannot be null");
        this.postProcessors.add(postProcessor);
    }

    @Override
    public boolean removePostProcessor(ProducerPostProcessor<K, V> postProcessor) {
        return this.postProcessors.remove(postProcessor);
    }

    @Override
    public void updateConfigs(Map<String, Object> updates) {
        updates.forEach((key, value) -> {
            if (key != null) {
                if (key.equals("transactional.id")) {
                    Assert.isTrue(value == null || value instanceof String,
                            () -> "'transactional.id' must be null or a String, not a " + value.getClass().getName());

                    Assert.isTrue(this.transactionIdPrefix != null == (value != null), "Cannot change transactional capability");
                    this.transactionIdPrefix = (String) value;
                } else if (key.equals("client.id")) {
                    Assert.isTrue(value == null || value instanceof String,
                            () -> "'client.id' must be null or a String, not a " + value.getClass().getName());

                    this.clientIdPrefix = (String) value;
                } else if (value != null) {
                    this.configs.put(key, value);
                }
            }
        });
    }

    @Override
    public void removeConfig(String configKey) {
        this.configs.remove(configKey);
    }

    private void enableIdempotentBehaviour() {
        Object previousValue = this.configs.putIfAbsent("enable.idempotence", true);
        if (Boolean.FALSE.equals(previousValue)) {
            LOGGER.debug(() -> "The 'enable.idempotence' is set to false, may result in duplicate messages");
        }
    }

    @Override
    public boolean transactionCapable() {
        return this.transactionIdPrefix != null;
    }

    public void destroy() {
        this.globalLock.lock();

        MockachuKafkaProducerFactory.CloseSafeProducer<K, V> producerToClose;
        try {
            producerToClose = this.producer;
            this.producer = null;
        } finally {
            this.globalLock.unlock();
        }

        if (producerToClose != null) {
            try {
                producerToClose.closeDelegate(this.physicalCloseTimeout);
            } catch (Exception e) {
                LOGGER.error(e, "Exception while closing producer");
            }
        }

        this.cache.values().forEach((queue) -> {
            for (MockachuKafkaProducerFactory.CloseSafeProducer<K, V> next = queue.poll();
                 next != null;
                 next = queue.poll()
            ) {
                try {
                    next.closeDelegate(this.physicalCloseTimeout);
                } catch (Exception e) {
                    LOGGER.error(e, "Exception while closing producer");
                }
            }
        });
        this.cache.clear();
        this.threadBoundProducers.values().forEach(prod -> {
            try {
                prod.closeDelegate(this.physicalCloseTimeout);
            } catch (Exception e) {
                LOGGER.error(e, "Exception while closing producer");
            }
        });
        this.threadBoundProducers.clear();
        this.epoch.incrementAndGet();
    }

    public void onApplicationEvent(ContextStoppedEvent event) {
        if (event.getApplicationContext().equals(this.applicationContext)) {
            this.reset();
        }
    }

    @Override
    public void reset() {
        try {
            this.destroy();
        } catch (Exception var2) {
            LOGGER.error(var2, "Exception while closing producer");
        }

    }

    public Producer<K, V> createProducer() {
        return this.createProducer(this.transactionIdPrefix);
    }

    @Override
    public Producer<K, V> createProducer(String txIdPrefixArg) {
        String txIdPrefix = txIdPrefixArg == null ? this.transactionIdPrefix : txIdPrefixArg;
        return this.doCreateProducer(txIdPrefix);
    }

    @Override
    public Producer<K, V> createNonTransactionalProducer() {
        return this.doCreateProducer(null);
    }

    private Producer<K, V> doCreateProducer(@Nullable String txIdPrefix) {
        if (txIdPrefix != null) {
            return this.createTransactionalProducer(txIdPrefix);
        } else if (this.producerPerThread) {
            return this.getOrCreateThreadBoundProducer();
        } else {
            this.globalLock.lock();

            MockachuKafkaProducerFactory.CloseSafeProducer<K, V> closeSafeProducer;
            try {
                if (this.producer != null && this.producer.closed) {
                    this.producer.closeDelegate(this.physicalCloseTimeout);
                    this.producer = null;
                }

                if (this.producer != null && this.expire(this.producer)) {
                    this.producer = null;
                }

                if (this.producer == null) {
                    this.producer = new MockachuKafkaProducerFactory.CloseSafeProducer<>(
                            this.createKafkaProducer(),
                            this::removeProducer,
                            this.physicalCloseTimeout,
                            this.beanName,
                            this.epoch.get());
                    this.listeners.forEach(listener -> listener.producerAdded(this.producer.clientId, this.producer));
                }

                closeSafeProducer = this.producer;
            } finally {
                this.globalLock.unlock();
            }

            return closeSafeProducer;
        }
    }

    private Producer<K, V> getOrCreateThreadBoundProducer() {
        MockachuKafkaProducerFactory.CloseSafeProducer<K, V> tlProducer = this.threadBoundProducers.get(Thread.currentThread());

        if (tlProducer != null && (tlProducer.closed || this.epoch.get() != tlProducer.epoch || this.expire(tlProducer))) {
            this.closeThreadBoundProducer();
            tlProducer = null;
        }

        if (tlProducer == null) {
            tlProducer = new MockachuKafkaProducerFactory.CloseSafeProducer<>(
                    this.createKafkaProducer(),
                    this::removeProducer,
                    this.physicalCloseTimeout,
                    this.beanName,
                    this.epoch.get());

            for (Listener<K, V> listener : this.listeners) {
                listener.producerAdded(tlProducer.clientId, tlProducer);
            }

            this.threadBoundProducers.put(Thread.currentThread(), tlProducer);
        }

        return tlProducer;
    }

    protected Producer<K, V> createKafkaProducer() {
        return this.createRawProducer(this.getProducerConfigs());
    }

    protected final boolean removeProducer(MockachuKafkaProducerFactory.CloseSafeProducer<K, V> producerToRemove, Duration timeout) {
        if (producerToRemove.closed) {
            this.listeners.forEach(listener -> listener.producerRemoved(producerToRemove.clientId, producerToRemove));
        }
        return producerToRemove.closed;
    }

    protected Producer<K, V> createTransactionalProducer() {
        return this.createTransactionalProducer(this.transactionIdPrefix);
    }

    protected Producer<K, V> createTransactionalProducer(String txIdPrefix) {
        BlockingQueue<MockachuKafkaProducerFactory.CloseSafeProducer<K, V>> queue = this.getCache(txIdPrefix);
        Assert.notNull(queue, () -> "No cache found for " + txIdPrefix);

        MockachuKafkaProducerFactory.CloseSafeProducer<K, V> cachedProducer;
        for (cachedProducer = queue.poll();
             cachedProducer != null;
             cachedProducer = queue.poll()) {
            this.expire(cachedProducer);
        }

        if (cachedProducer == null) {
            String suffix = this.transactionIdSuffixStrategy.acquireSuffix(txIdPrefix);
            return this.doCreateTxProducer(txIdPrefix, suffix, this::cacheReturner);
        } else {
            return cachedProducer;
        }
    }

    private boolean expire(MockachuKafkaProducerFactory.CloseSafeProducer<K, V> producer) {
        boolean expired = this.maxAge > 0L && System.currentTimeMillis() - producer.created > this.maxAge;
        if (expired) {
            producer.closeDelegate(this.physicalCloseTimeout);
        }
        return expired;
    }

    boolean cacheReturner(MockachuKafkaProducerFactory.CloseSafeProducer<K, V> producerToRemove, Duration timeout) {
        if (producerToRemove.closed) {
            this.removeTransactionProducer(producerToRemove, timeout, this.listeners);
            return true;
        } else {
            this.globalLock.lock();

            try {
                if (producerToRemove.epoch != this.epoch.get()) {
                    this.removeTransactionProducer(producerToRemove, timeout, this.listeners);
                    return true;
                }

                BlockingQueue<MockachuKafkaProducerFactory.CloseSafeProducer<K, V>> txIdCache = this.getCache(producerToRemove.txIdPrefix);
                if (producerToRemove.epoch != this.epoch.get() || txIdCache != null && !txIdCache.contains(producerToRemove) && !txIdCache.offer(producerToRemove)) {
                    this.removeTransactionProducer(producerToRemove, timeout, this.listeners);
                    return true;
                }
            } finally {
                this.globalLock.unlock();
            }

            return false;
        }
    }

    private void removeTransactionProducer(MockachuKafkaProducerFactory.CloseSafeProducer<K, V> producer,
                                           Duration timeout,
                                           List<Listener<K, V>> listeners) {
        this.transactionIdSuffixStrategy.releaseSuffix(producer.txIdPrefix, producer.txIdSuffix);
        listeners.forEach(listener -> listener.producerRemoved(producer.clientId, producer));
    }

    private MockachuKafkaProducerFactory.CloseSafeProducer<K, V> doCreateTxProducer(
            String prefix,
            String suffix,
            BiPredicate<MockachuKafkaProducerFactory.CloseSafeProducer<K, V>, Duration> remover
    ) {
        Producer<K, V> newProducer = this.createRawProducer(this.getTxProducerConfigs(prefix + suffix));

        try {
            newProducer.initTransactions();
        } catch (RuntimeException e) {
            try {
                newProducer.close(this.physicalCloseTimeout);
            } catch (RuntimeException suppressed) {
                KafkaException newEx = new KafkaException("initTransactions() failed and then close() failed", e);
                newEx.addSuppressed(suppressed);
                throw newEx;
            } finally {
                this.transactionIdSuffixStrategy.releaseSuffix(prefix, suffix);
            }

            throw new KafkaException("initTransactions() failed", e);
        }

        MockachuKafkaProducerFactory.CloseSafeProducer<K, V> closeSafeProducer =
                new MockachuKafkaProducerFactory.CloseSafeProducer<>(
                        newProducer, remover, prefix, suffix, this.physicalCloseTimeout, this.beanName, this.epoch.get());

        this.listeners.forEach(listener -> listener.producerAdded(closeSafeProducer.clientId, closeSafeProducer));
        return closeSafeProducer;
    }

    protected Producer<K, V> createRawProducer(Map<String, Object> rawConfigs) {
        Producer<K, V> kafkaProducer =
                new MockachuKafkaProducer<>(rawConfigs,
                        this.keySerializerSupplier.get(),
                        this.valueSerializerSupplier.get(),
                        sender);

        ProducerPostProcessor<K, V> pp;
        for (Iterator<ProducerPostProcessor<K, V>> it = this.postProcessors.iterator();
             it.hasNext();
             kafkaProducer = pp.apply(kafkaProducer)
        ) {
            pp = it.next();
        }

        return kafkaProducer;
    }

    @Nullable
    protected BlockingQueue<MockachuKafkaProducerFactory.CloseSafeProducer<K, V>> getCache() {
        return this.getCache(this.transactionIdPrefix);
    }

    @Nullable
    protected BlockingQueue<MockachuKafkaProducerFactory.CloseSafeProducer<K, V>> getCache(@Nullable String txIdPrefix) {
        return txIdPrefix == null ? null : this.cache.computeIfAbsent(txIdPrefix, txId -> new LinkedBlockingQueue<>());
    }

    @Override
    public void closeThreadBoundProducer() {
        MockachuKafkaProducerFactory.CloseSafeProducer<K, V> tlProducer = this.threadBoundProducers.remove(Thread.currentThread());
        if (tlProducer != null) {
            tlProducer.closeDelegate(this.physicalCloseTimeout);
        }
    }

    protected Map<String, Object> getProducerConfigs() {
        Map<String, Object> newProducerConfigs = new HashMap<>(this.configs);
        this.checkBootstrap(newProducerConfigs);
        String prefix;
        if (this.clientIdPrefix != null) {
            prefix = this.clientIdPrefix;
        } else {
            prefix = Optional.ofNullable(this.applicationContext)
                    .map(EnvironmentCapable::getEnvironment)
                    .map(environment -> environment.getProperty("spring.application.name"))
                    .map(applicationName -> applicationName + "-producer").orElse(null);
        }

        if (prefix != null) {
            newProducerConfigs.put("client.id", prefix + "-" + this.clientIdCounter.incrementAndGet());
        }

        return newProducerConfigs;
    }

    protected Map<String, Object> getTxProducerConfigs(String transactionId) {
        Map<String, Object> newProducerConfigs = this.getProducerConfigs();
        newProducerConfigs.put("transactional.id", transactionId);
        return newProducerConfigs;
    }

    protected static class CloseSafeProducer<K, V> implements Producer<K, V> {
        private static final Duration CLOSE_TIMEOUT_AFTER_TX_TIMEOUT = Duration.ofMillis(0L);
        final String txIdPrefix;
        final String txIdSuffix;
        final long created;
        final String clientId;
        final int epoch;
        private final Producer<K, V> delegate;
        private final BiPredicate<MockachuKafkaProducerFactory.CloseSafeProducer<K, V>, Duration> removeProducer;
        private final Duration closeTimeout;
        volatile boolean closed;
        private volatile Exception producerFailed;

        CloseSafeProducer(Producer<K, V> delegate, BiPredicate<MockachuKafkaProducerFactory.CloseSafeProducer<K, V>, Duration> removeConsumerProducer, Duration closeTimeout, String factoryName, int epoch) {
            this(delegate, removeConsumerProducer, null, closeTimeout, factoryName, epoch);
        }

        CloseSafeProducer(Producer<K, V> delegate, BiPredicate<MockachuKafkaProducerFactory.CloseSafeProducer<K, V>, Duration> removeProducer, @Nullable String txIdPrefix, Duration closeTimeout, String factoryName, int epoch) {
            this(delegate, removeProducer, txIdPrefix, null, closeTimeout, factoryName, epoch);
        }

        CloseSafeProducer(Producer<K, V> delegate, BiPredicate<MockachuKafkaProducerFactory.CloseSafeProducer<K, V>, Duration> removeProducer, @Nullable String txIdPrefix, @Nullable String txIdSuffix, Duration closeTimeout, String factoryName, int epoch) {
            Assert.isTrue(!(delegate instanceof MockachuKafkaProducerFactory.CloseSafeProducer), "Cannot double-wrap a producer");
            this.delegate = delegate;
            this.removeProducer = removeProducer;
            this.txIdPrefix = txIdPrefix;
            this.txIdSuffix = txIdSuffix;
            this.closeTimeout = closeTimeout;
            Map<MetricName, ? extends Metric> metrics = delegate.metrics();
            Iterator<MetricName> metricIterator = metrics.keySet().iterator();
            String id;
            if (metricIterator.hasNext()) {
                id = (metricIterator.next()).tags().get("client-id");
            } else {
                id = "unknown";
            }

            this.clientId = factoryName + "." + id;
            this.created = System.currentTimeMillis();
            this.epoch = epoch;
            MockachuKafkaProducerFactory.LOGGER.debug(() -> "Created new Producer: " + this);
        }

        Producer<K, V> getDelegate() {
            return this.delegate;
        }

        public Future<RecordMetadata> send(ProducerRecord<K, V> producerRecord) {
            MockachuKafkaProducerFactory.LOGGER.trace(() -> this + " send(" + producerRecord + ")");
            return this.delegate.send(producerRecord);
        }

        public Future<RecordMetadata> send(ProducerRecord<K, V> producerRecord, Callback callback) {
            MockachuKafkaProducerFactory.LOGGER.trace(() -> this + " send(" + producerRecord + ")");
            return this.delegate.send(producerRecord, (metadata, exception) -> {
                if (exception instanceof OutOfOrderSequenceException) {
                    CloseSafeProducer.this.producerFailed = exception;
                    CloseSafeProducer.this.close(CloseSafeProducer.this.closeTimeout);
                }
                callback.onCompletion(metadata, exception);
            });
        }

        public void flush() {
            MockachuKafkaProducerFactory.LOGGER.trace(() -> this + " flush()");
            this.delegate.flush();
        }

        public List<PartitionInfo> partitionsFor(String topic) {
            return this.delegate.partitionsFor(topic);
        }

        public Map<MetricName, ? extends Metric> metrics() {
            return this.delegate.metrics();
        }

        public Uuid clientInstanceId(Duration timeout) {
            return this.delegate.clientInstanceId(timeout);
        }

        public void initTransactions() {
            this.delegate.initTransactions();
        }

        public void beginTransaction() throws ProducerFencedException {
            MockachuKafkaProducerFactory.LOGGER.debug(() -> this + " beginTransaction()");

            try {
                this.delegate.beginTransaction();
            } catch (RuntimeException e) {
                MockachuKafkaProducerFactory.LOGGER.error(e, () -> "beginTransaction failed: " + this);
                this.producerFailed = e;
                throw e;
            }
        }

        public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets, String consumerGroupId) throws ProducerFencedException {
            MockachuKafkaProducerFactory.LOGGER.trace(
                    () -> this + " sendOffsetsToTransaction(" + offsets + ", " + consumerGroupId + ")");
            this.delegate.sendOffsetsToTransaction(offsets, consumerGroupId);
        }

        public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets, ConsumerGroupMetadata groupMetadata) throws ProducerFencedException {
            MockachuKafkaProducerFactory.LOGGER.trace(
                    () -> this + " sendOffsetsToTransaction(" + offsets + ", " + groupMetadata + ")");
            this.delegate.sendOffsetsToTransaction(offsets, groupMetadata);
        }

        public void commitTransaction() throws ProducerFencedException {
            MockachuKafkaProducerFactory.LOGGER.debug(() -> this + " commitTransaction()");

            try {
                this.delegate.commitTransaction();
            } catch (RuntimeException e) {
                MockachuKafkaProducerFactory.LOGGER.error(e, () -> "commitTransaction failed: " + this);
                this.producerFailed = e;
                throw e;
            }
        }

        public void abortTransaction() throws ProducerFencedException {
            MockachuKafkaProducerFactory.LOGGER.debug(() -> this + " abortTransaction()");
            if (this.producerFailed != null) {
                MockachuKafkaProducerFactory.LOGGER.debug(
                        () -> "abortTransaction ignored - previous txFailed: " + this.producerFailed.getMessage() + ": " + this);
            } else {
                try {
                    this.delegate.abortTransaction();
                } catch (RuntimeException e) {
                    MockachuKafkaProducerFactory.LOGGER.error(e, () -> "Abort failed: " + this);
                    this.producerFailed = e;
                    throw e;
                }
            }

        }

        public void close() {
            this.close(null);
        }

        public void close(@Nullable Duration timeout) {
            MockachuKafkaProducerFactory.LOGGER.trace(() -> this + " close(" + (timeout == null ? "null" : timeout) + ")");
            if (!this.closed) {
                if (this.producerFailed != null) {
                    MockachuKafkaProducerFactory.LOGGER.warn(
                            () -> "Error during some operation; producer removed from cache: " + this);
                    this.closed = true;
                    this.removeProducer.test(this, this.producerFailed instanceof TimeoutException ? CLOSE_TIMEOUT_AFTER_TX_TIMEOUT : timeout);
                    this.delegate.close(timeout == null ? this.closeTimeout : (this.producerFailed instanceof TimeoutException ? CLOSE_TIMEOUT_AFTER_TX_TIMEOUT : timeout));
                } else {
                    this.closed = this.removeProducer.test(this, timeout);
                    if (this.closed) {
                        this.delegate.close(timeout == null ? this.closeTimeout : timeout);
                    }
                }
            }
        }

        void closeDelegate(Duration timeout) {
            try {
                if (!this.closed) {
                    this.delegate.close(timeout == null ? this.closeTimeout : timeout);
                    this.closed = true;
                    this.removeProducer.test(this, timeout == null ? this.closeTimeout : timeout);
                }
            } catch (Exception var3) {
                MockachuKafkaProducerFactory.LOGGER.warn(var3, () -> "Failed to close " + this.delegate);
            }

        }

        public String toString() {
            return "CloseSafeProducer [delegate=" + this.delegate + "]";
        }
    }
}
