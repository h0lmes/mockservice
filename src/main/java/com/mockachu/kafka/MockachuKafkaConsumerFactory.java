package com.mockachu.kafka;

import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.log.LogAccessor;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ConsumerPostProcessor;
import org.springframework.kafka.core.KafkaResourceFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class MockachuKafkaConsumerFactory<K, V> extends KafkaResourceFactory implements ConsumerFactory<K, V>, BeanNameAware, ApplicationContextAware {
    private static final LogAccessor LOGGER = new LogAccessor(LogFactory.getLog(MockachuKafkaConsumerFactory.class));
    private final Map<String, Object> configs;
    private final List<Listener<K, V>> listeners;
    private final List<ConsumerPostProcessor<K, V>> postProcessors;
    private Supplier<Deserializer<K>> keyDeserializerSupplier;
    private Supplier<Deserializer<V>> valueDeserializerSupplier;
    private String beanName;
    private boolean configureDeserializers;
    private ApplicationContext applicationContext;

    public MockachuKafkaConsumerFactory(Map<String, Object> configs) {
        this(configs, () -> null, () -> null);
    }

    public MockachuKafkaConsumerFactory(Map<String, Object> configs, @Nullable Deserializer<K> keyDeserializer, @Nullable Deserializer<V> valueDeserializer) {
        this(configs, () -> keyDeserializer, () -> valueDeserializer);
    }

    public MockachuKafkaConsumerFactory(Map<String, Object> configs, @Nullable Deserializer<K> keyDeserializer, @Nullable Deserializer<V> valueDeserializer, boolean configureDeserializers) {
        this(configs, () -> keyDeserializer, () -> valueDeserializer, configureDeserializers);
    }

    public MockachuKafkaConsumerFactory(Map<String, Object> configs, @Nullable Supplier<Deserializer<K>> keyDeserializerSupplier, @Nullable Supplier<Deserializer<V>> valueDeserializerSupplier) {
        this(configs, keyDeserializerSupplier, valueDeserializerSupplier, true);
    }

    public MockachuKafkaConsumerFactory(Map<String, Object> configs, @Nullable Supplier<Deserializer<K>> keyDeserializerSupplier, @Nullable Supplier<Deserializer<V>> valueDeserializerSupplier, boolean configureDeserializers) {
        this.listeners = new ArrayList<>();
        this.postProcessors = new ArrayList<>();
        this.beanName = "not.managed.by.Spring";
        this.configureDeserializers = true;
        this.configs = new ConcurrentHashMap<>(configs);
        this.configureDeserializers = configureDeserializers;
        this.keyDeserializerSupplier = keyDeserializerSupplier;
        this.valueDeserializerSupplier = valueDeserializerSupplier;
    }

    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void setKeyDeserializerSupplier(Supplier<Deserializer<K>> keyDeserializerSupplier) {
        this.keyDeserializerSupplier = keyDeserializerSupplier;
    }

    public void setValueDeserializerSupplier(Supplier<Deserializer<V>> valueDeserializerSupplier) {
        this.valueDeserializerSupplier = valueDeserializerSupplier;
    }

    public void setConfigureDeserializers(boolean configureDeserializers) {
        this.configureDeserializers = configureDeserializers;
    }

    public Map<String, Object> getConfigurationProperties() {
        Map<String, Object> configs2 = new HashMap<>(this.configs);
        this.checkBootstrap(configs2);
        return Collections.unmodifiableMap(configs2);
    }

    public Deserializer<K> getKeyDeserializer() {
        return this.keyDeserializerSupplier.get();
    }

    public void setKeyDeserializer(@Nullable Deserializer<K> keyDeserializer) {
        this.keyDeserializerSupplier = () -> keyDeserializer;
    }

    public Deserializer<V> getValueDeserializer() {
        return this.valueDeserializerSupplier.get();
    }

    public void setValueDeserializer(@Nullable Deserializer<V> valueDeserializer) {
        this.valueDeserializerSupplier = () -> valueDeserializer;
    }

    public List<Listener<K, V>> getListeners() {
        return Collections.unmodifiableList(this.listeners);
    }

    public List<ConsumerPostProcessor<K, V>> getPostProcessors() {
        return Collections.unmodifiableList(this.postProcessors);
    }

    public void addListener(Listener<K, V> listener) {
        Assert.notNull(listener, "'listener' cannot be null");
        this.listeners.add(listener);
    }

    public void addListener(int index, Listener<K, V> listener) {
        Assert.notNull(listener, "'listener' cannot be null");
        if (index >= this.listeners.size()) {
            this.listeners.add(listener);
        } else {
            this.listeners.add(index, listener);
        }
    }

    public void addPostProcessor(ConsumerPostProcessor<K, V> postProcessor) {
        Assert.notNull(postProcessor, "'postProcessor' cannot be null");
        this.postProcessors.add(postProcessor);
    }

    public boolean removePostProcessor(ConsumerPostProcessor<K, V> postProcessor) {
        return this.postProcessors.remove(postProcessor);
    }

    public boolean removeListener(Listener<K, V> listener) {
        return this.listeners.remove(listener);
    }

    public void updateConfigs(Map<String, Object> updates) {
        this.configs.putAll(updates);
    }

    public void removeConfig(String configKey) {
        this.configs.remove(configKey);
    }

    public Consumer<K, V> createConsumer(@Nullable String groupId,
                                         @Nullable String clientIdPrefix,
                                         @Nullable final String clientIdSuffixArg,
                                         @Nullable Properties properties) {
        return this.createKafkaConsumer(groupId, clientIdPrefix, clientIdSuffixArg, properties);
    }

    protected Consumer<K, V> createKafkaConsumer(@Nullable String groupId,
                                                 @Nullable String clientIdPrefixArg,
                                                 @Nullable String clientIdSuffixArg,
                                                 @Nullable Properties properties) {
        boolean overrideClientIdPrefix = StringUtils.hasText(clientIdPrefixArg);
        String clientIdPrefix = clientIdPrefixArg;
        String clientIdSuffix = clientIdSuffixArg;
        if (clientIdPrefixArg == null) {
            clientIdPrefix = "";
        }

        if (clientIdSuffixArg == null) {
            clientIdSuffix = "";
        }

        boolean hasGroupIdOrClientIdInProperties = properties != null &&
                (properties.containsKey("client.id") || properties.containsKey("group.id"));
        boolean hasGroupIdOrClientIdInConfig = this.configs.containsKey("client.id") || this.configs.containsKey("group.id");
        if (!overrideClientIdPrefix && groupId == null && !hasGroupIdOrClientIdInProperties && !hasGroupIdOrClientIdInConfig) {
            String applicationName = Optional.ofNullable(this.applicationContext)
                    .map(EnvironmentCapable::getEnvironment)
                    .map(environment -> environment.getProperty("spring.application.name"))
                    .orElse(null);
            if (applicationName != null) {
                clientIdPrefix = applicationName + "-consumer";
                overrideClientIdPrefix = true;
            }
        }

        boolean shouldModifyClientId = this.configs.containsKey("client.id") &&
                StringUtils.hasText(clientIdSuffix) || overrideClientIdPrefix;
        return groupId == null &&
                (properties == null || properties.stringPropertyNames().isEmpty()) &&
                !shouldModifyClientId ?
                this.createKafkaConsumer(new HashMap<>(this.configs)) :
                this.createConsumerWithAdjustedProperties(
                        groupId, clientIdPrefix, properties, overrideClientIdPrefix, clientIdSuffix, shouldModifyClientId);
    }

    private Consumer<K, V> createConsumerWithAdjustedProperties(@Nullable String groupId, String clientIdPrefix,
                                                                @Nullable Properties properties,
                                                                boolean overrideClientIdPrefix,
                                                                String clientIdSuffix, boolean shouldModifyClientId) {
        Map<String, Object> modifiedConfigs = new HashMap<>(this.configs);
        if (groupId != null) {
            modifiedConfigs.put("group.id", groupId);
        }

        if (shouldModifyClientId) {
            String var10002 = String.valueOf(overrideClientIdPrefix ? clientIdPrefix : modifiedConfigs.get("client.id"));
            modifiedConfigs.put("client.id", var10002 + clientIdSuffix);
        }

        if (properties != null) {
            Set<String> stringPropertyNames = properties.stringPropertyNames();
            stringPropertyNames.stream().filter(name ->
                    !name.equals("client.id") && !name.equals("group.id")
            ).forEach(name ->
                    modifiedConfigs.put(name, properties.getProperty(name))
            );
            properties.entrySet().stream().filter(entry ->
                    !entry.getKey().equals("client.id") && !entry.getKey().equals("group.id") && !stringPropertyNames.contains(entry.getKey()) && entry.getKey() instanceof String
            ).forEach(entry ->
                    modifiedConfigs.put((String) entry.getKey(), entry.getValue())
            );
            this.checkInaccessible(properties, modifiedConfigs);
        }

        return this.createKafkaConsumer(modifiedConfigs);
    }

    private void checkInaccessible(Properties properties, Map<String, Object> modifiedConfigs) {
        List<Object> inaccessible = null;
        Enumeration<?> propertyNames = properties.propertyNames();

        while (propertyNames.hasMoreElements()) {
            Object nextElement = propertyNames.nextElement();
            if (!modifiedConfigs.containsKey(nextElement)) {
                if (inaccessible == null) {
                    inaccessible = new ArrayList<>();
                }

                inaccessible.add(nextElement);
            }
        }

        if (inaccessible != null) {
            LOGGER.error("Non-String-valued default properties are inaccessible; use String values or make them explicit properties instead of defaults: " + String.valueOf(inaccessible));
        }
    }

    protected Consumer<K, V> createKafkaConsumer(Map<String, Object> configProps) {
        this.checkBootstrap(configProps);
        Consumer<K, V> kafkaConsumer = this.createRawConsumer(configProps);
        if (!this.listeners.isEmpty() && !(kafkaConsumer instanceof MockachuKafkaConsumerFactory.ExtendedKafkaConsumer)) {
            LOGGER.warn("The 'ConsumerFactory.Listener' configuration is ignored because the consumer is not an instance of 'ExtendedKafkaConsumer'.Consider extending 'ExtendedKafkaConsumer' or implement your own 'ConsumerFactory'.");
        }

        ConsumerPostProcessor<K, V> pp;
        for (Iterator<ConsumerPostProcessor<K, V>> var3 = this.postProcessors.iterator();
             var3.hasNext();
             kafkaConsumer = pp.apply(kafkaConsumer)
        ) {
            pp = var3.next();
        }

        return kafkaConsumer;
    }

    protected Consumer<K, V> createRawConsumer(Map<String, Object> configProps) {
        return new MockachuKafkaConsumerFactory<K, V>.ExtendedKafkaConsumer(configProps);
    }

    public boolean isAutoCommit() {
        Object auto = this.configs.get("enable.auto.commit");
        return auto instanceof Boolean boolAuto ? boolAuto : !(auto instanceof String strAuto) || Boolean.parseBoolean(strAuto);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Nullable
    private Deserializer<K> keyDeserializer(Map<String, Object> configs) {
        Deserializer<K> deserializer = this.keyDeserializerSupplier != null ? this.keyDeserializerSupplier.get() : null;
        if (deserializer != null && this.configureDeserializers) {
            deserializer.configure(configs, true);
        }
        return deserializer;
    }

    @Nullable
    private Deserializer<V> valueDeserializer(Map<String, Object> configs) {
        Deserializer<V> deserializer = this.valueDeserializerSupplier != null ? this.valueDeserializerSupplier.get() : null;
        if (deserializer != null && this.configureDeserializers) {
            deserializer.configure(configs, false);
        }
        return deserializer;
    }

    protected class ExtendedKafkaConsumer extends MockachuKafkaConsumer<K, V> {
        private String idForListeners;

        protected ExtendedKafkaConsumer(Map<String, Object> configProps) {
            super(new ConsumerConfig(configProps),
                    MockachuKafkaConsumerFactory.this.keyDeserializer(configProps),
                    MockachuKafkaConsumerFactory.this.valueDeserializer(configProps));

            if (!MockachuKafkaConsumerFactory.this.listeners.isEmpty()) {
                Iterator<MetricName> metricIterator = this.metrics().keySet().iterator();
                String clientId = "unknown";
                if (metricIterator.hasNext()) {
                    clientId = (metricIterator.next()).tags().get("client-id");
                }

                this.idForListeners = MockachuKafkaConsumerFactory.this.beanName + "." + clientId;
                for (Listener<K, V> listener : MockachuKafkaConsumerFactory.this.listeners) {
                    listener.consumerAdded(this.idForListeners, this);
                }
            }
        }

        @Override
        public void close() {
            super.close();
            this.notifyConsumerRemoved();
        }

        @Override
        public void close(Duration timeout) {
            super.close(timeout);
            this.notifyConsumerRemoved();
        }

        private void notifyConsumerRemoved() {
            for (Listener<K, V> listener : MockachuKafkaConsumerFactory.this.listeners) {
                listener.consumerRemoved(this.idForListeners, this);
            }
        }
    }
}
