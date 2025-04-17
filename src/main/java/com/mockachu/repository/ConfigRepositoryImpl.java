package com.mockachu.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockachu.domain.*;
import com.mockachu.exception.KafkaTopicAlreadyExistsException;
import com.mockachu.exception.RouteAlreadyExistsException;
import com.mockachu.exception.ScenarioAlreadyExistsException;
import com.mockachu.exception.TestAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class ConfigRepositoryImpl implements ConfigRepository {

    private static final Logger log = LoggerFactory.getLogger(ConfigRepositoryImpl.class);

    private static final String REQUEST_COULD_NOT_BE_NULL = "Request could not be null.";
    private static final String TEST_COULD_NOT_BE_NULL = "Test could not be null.";
    private final String fileConfigPath;
    private final String fileConfigBackupPath;
    private final ObjectMapper yamlMapper;
    private final List<ConfigObserver> configObservers = new ArrayList<>();
    private final List<RouteObserver> routeObservers = new ArrayList<>();
    private final List<OutboundRequestObserver> requestObservers = new ArrayList<>();
    private final List<SettingsObserver> settingsObservers = new ArrayList<>();
    private Config config;

    public ConfigRepositoryImpl(
            @Value("${application.config-filename}") String fileConfigPath,
            @Value("${application.config-backup-filename}") String fileConfigBackupPath,
            @Qualifier("yamlMapper") ObjectMapper yamlMapper
    ) {
        this.fileConfigPath = fileConfigPath;
        this.fileConfigBackupPath = fileConfigBackupPath;
        this.yamlMapper = yamlMapper.copy().setMixIns(getMixIns());

        try {
            readConfigFromFile();
        } catch (IOException e) {
            log.warn("Could not read config file {}. Using empty config.", this.fileConfigPath);
            config = new Config();
        }
    }

    @Override
    public void registerConfigObserver(ConfigObserver observer) {
        this.configObservers.add(observer);
    }

    @Override
    public void registerRouteObserver(RouteObserver observer) {
        this.routeObservers.add(observer);
    }

    @Override
    public void registerRequestObserver(OutboundRequestObserver observer) {
        requestObservers.add(observer);
    }

    @Override
    public void registerSettingsObserver(SettingsObserver observer) {
        this.settingsObservers.add(observer);
    }

    private Map<Class<?>, Class<?>> getMixIns() {
        Map<Class<?>, Class<?>> mixins = new HashMap<>();
        mixins.put(Scenario.class, Scenario.MixInIgnoreIdActive.class);
        return mixins;
    }

    private void readConfigFromFile() throws IOException {
        readConfigFromFile(getConfigFile());
    }

    private void readConfigFromFile(File file) throws IOException {
        config = yamlMapper.readValue(file, Config.class);
        if (config == null) {
            throw new IOException("Mapper returned null Config.");
        }
        sortRoutes();
        sortRequests();
        sortTests();
        sortScenarios();
    }

    private void configFromString(String yaml) throws IOException {
        try {
            config = yamlMapper.readValue(yaml, Config.class);
            if (config == null) {
                throw new IOException("Mapper returned null Config.");
            }
            sortRoutes();
            sortRequests();
            sortTests();
            sortScenarios();
        } catch (IOException e) {
            throw new IOException("Could not deserialize config. " + e.getMessage(), e);
        }
    }

    private void sortRoutes() {
        config.getRoutes().sort(Route::compareTo);
    }

    private void sortScenarios() {
        config.getScenarios().sort(Scenario::compareTo);
    }

    private void sortRequests() {
        config.getRequests().sort(OutboundRequest::compareTo);
    }

    private void sortTests() {
        config.getTests().sort(ApiTest::compareTo);
    }

    private void sortKafkaTopics() {
        config.getKafkaTopics().sort(KafkaTopic::compareTo);
    }

    private void tryPersistConfig() throws IOException {
        tryPersistConfig(getConfigFile());
    }

    private void tryPersistConfig(File file) throws IOException {
        try {
            yamlMapper.writeValue(file, config);
        } catch (IOException e) {
            throw new IOException("Could not write config to file. " + e.getMessage(), e);
        }
    }

    private String configToString() throws JsonProcessingException {
        return yamlMapper.writeValueAsString(config);
    }

    private File getConfigFile() {
        return new File(fileConfigPath);
    }

    private File getConfigBackupFile() {
        return new File(fileConfigBackupPath);
    }

    //----------------------------------------------------------------------
    //
    //   Config
    //
    //----------------------------------------------------------------------

    @Override
    public synchronized String getConfigData() throws JsonProcessingException {
        return configToString();
    }

    @Override
    public synchronized void writeConfigData(String data) throws IOException {
        notifyBeforeConfigChanged();
        configFromString(data);
        notifyAfterConfigChanged();
        notifySettingsChanged();
        tryPersistConfig();
    }

    @Override
    public synchronized void backup() throws IOException {
        tryPersistConfig(getConfigBackupFile());
    }

    @Override
    public synchronized void restore() throws IOException {
        notifyBeforeConfigChanged();
        readConfigFromFile(getConfigBackupFile());
        notifyAfterConfigChanged();
        notifySettingsChanged();
        tryPersistConfig();
    }

    private void notifyBeforeConfigChanged() {
        configObservers.forEach(ConfigObserver::onBeforeConfigChanged);
    }

    private void notifyAfterConfigChanged() {
        configObservers.forEach(ConfigObserver::onAfterConfigChanged);
    }

    //----------------------------------------------------------------------
    //
    //   Settings
    //
    //----------------------------------------------------------------------

    @Override
    public synchronized Settings getSettings() {
        return config.getSettings();
    }

    @Override
    public synchronized void setSettings(Settings settings) throws IOException {
        config.setSettings(settings);
        notifySettingsChanged();
        tryPersistConfig();
    }

    private void notifySettingsChanged() {
        settingsObservers.forEach(SettingsObserver::onAfterSettingsChanged);
    }

    //----------------------------------------------------------------------
    //
    //   Routes
    //
    //----------------------------------------------------------------------

    @Override
    public List<Route> findAllRoutes() {
        return config.getRoutes();
    }

    @Override
    public Optional<Route> findRoute(@Nullable Route route) {
        if (route == null) {
            return Optional.empty();
        }
        return findAllRoutes().stream()
                .filter(route::equals)
                .findFirst();
    }

    @Override
    public void putRoute(@Nullable Route originalRoute, @Nonnull Route route) throws IOException {
        Objects.requireNonNull(route, "Route could not be null.");
        putRouteToConfig(originalRoute, route);
        sortRoutes();
        tryPersistConfig();
    }

    private void putRouteToConfig(Route originalRoute, Route route) {
        Route existingOriginal = findRoute(originalRoute).orElse(null);
        if (existingOriginal == null) {
            putRouteNew(route);
        } else {
            putRouteExisting(route, existingOriginal);
        }
    }

    private void putRouteNew(Route route) {
        Route existing = findRoute(route).orElse(null);
        if (existing != null) {
            throw new RouteAlreadyExistsException(route);
        }
        config.getRoutes().add(route);
        notifyRouteCreated(route);
    }

    private void putRouteExisting(Route route, Route existingOriginal) {
        Route existingRoute = findRoute(route).orElse(null);
        if (existingRoute != null && !existingOriginal.equals(existingRoute)) {
            throw new RouteAlreadyExistsException(route);
        }
        notifyRouteDeleted(existingOriginal);
        existingOriginal.assignFrom(route);
        notifyRouteCreated(existingOriginal);
    }

    @Override
    public void putRoutes(List<Route> routes, boolean overwrite) throws IOException {
        boolean modified = false;

        for (Route route : routes) {
            modified |= putRouteInternal(route, overwrite);
        }

        if (modified) {
            sortRoutes();
            tryPersistConfig();
        }
    }

    private boolean putRouteInternal(@Nonnull Route route, boolean overwrite) {
        Objects.requireNonNull(route, "Route could not be null.");
        Route existing = findRoute(route).orElse(null);

        if (existing == null) {
            config.getRoutes().add(route);
            notifyRouteCreated(route);
            return true;
        } else if (overwrite) {
            notifyRouteDeleted(existing);
            existing.assignFrom(route);
            notifyRouteCreated(existing);
            return true;
        }
        return false;
    }

    @Override
    public void deleteRoutes(List<Route> routes) throws IOException {
        boolean modified = false;

        for (Route route : routes) {
            if (config.getRoutes().remove(route)) {
                notifyRouteDeleted(route);
                modified = true;
            }
        }

        if (modified) {
            tryPersistConfig();
        }
    }

    private void notifyRouteCreated(Route route) {
        routeObservers.forEach(o -> o.onRouteCreated(route));
    }

    private void notifyRouteDeleted(Route route) {
        routeObservers.forEach(o -> o.onRouteDeleted(route));
    }

    //----------------------------------------------------------------------
    //
    //   Scenarios
    //
    //----------------------------------------------------------------------

    @Override
    public List<Scenario> findAllScenarios() {
        return config.getScenarios();
    }

    @Override
    public Optional<Scenario> findScenario(@Nullable Scenario scenario) {
        if (scenario == null) {
            return Optional.empty();
        }
        return findAllScenarios().stream()
                .filter(scenario::equals)
                .findFirst();
    }

    @Override
    public void putScenario(@Nullable Scenario originalScenario, @Nonnull Scenario scenario) throws IOException {
        Objects.requireNonNull(scenario, "Scenario could not be null.");
        putScenarioToConfig(originalScenario, scenario);
        sortScenarios();
        tryPersistConfig();
    }

    private void putScenarioToConfig(Scenario originalScenario, Scenario scenario) {
        Scenario existingOriginal = findScenario(originalScenario).orElse(null);
        if (existingOriginal == null) {
            putScenarioNew(scenario);
        } else {
            putScenarioExisting(scenario, existingOriginal);
        }
    }

    private void putScenarioNew(Scenario scenario) {
        Scenario existing = findScenario(scenario).orElse(null);
        if (existing != null) {
            throw new ScenarioAlreadyExistsException(scenario);
        }
        config.getScenarios().add(scenario);
    }

    private void putScenarioExisting(Scenario scenario, Scenario existingOriginal) {
        Scenario existingScenario = findScenario(scenario).orElse(null);
        if (existingScenario != null && !existingOriginal.equals(existingScenario)) {
            throw new ScenarioAlreadyExistsException(scenario);
        }
        existingOriginal.assignFrom(scenario);
    }

    @Override
    public void deleteScenario(Scenario scenario) throws IOException {
        if (config.getScenarios().remove(scenario)) {
            tryPersistConfig();
        }
    }

    //----------------------------------------------------------------------
    //
    //   Requests
    //
    //----------------------------------------------------------------------

    @Override
    public List<OutboundRequest> findAllRequests() {
        return config.getRequests();
    }

    @Override
    public Optional<OutboundRequest> findRequest(@Nullable String requestId) {
        if (requestId == null) {
            return Optional.empty();
        }
        return findAllRequests().stream()
                .filter(r -> requestId.trim().equals(r.getId()))
                .findFirst();
    }

    @Override
    public void putRequest(@Nullable OutboundRequest existing, @Nonnull OutboundRequest request) throws IOException {
        Objects.requireNonNull(request, REQUEST_COULD_NOT_BE_NULL);

        existing = findRequest(existing == null ? null : existing.getId()).orElse(null);
        if (existing == null) {
            ensureUniqueRequestId(null, request);
            config.getRequests().add(request);
            notifyRequestCreated(request);
        } else {
            ensureUniqueRequestId(existing.getId(), request);
            notifyRequestDeleted(existing);
            existing.assignFrom(request);
            notifyRequestCreated(existing);
        }

        sortRequests();
        tryPersistConfig();
    }

    private void ensureUniqueRequestId(@Nullable String previousId, @Nonnull OutboundRequest request) {
        Objects.requireNonNull(request, REQUEST_COULD_NOT_BE_NULL);
        if (previousId != null && previousId.equals(request.getId())) return;

        if (request.getId() != null
                && !request.getId().isBlank()
                && findRequest(request.getId()).isEmpty()) return;

        String baseId = request.generateId();
        int index = 0;
        String fullId = baseId;
        while (findRequest(fullId).isPresent()) {
            index++;
            fullId = baseId + "." + index;
        }
        request.setId(fullId);
    }

    @Override
    public void putRequests(List<OutboundRequest> requests, boolean overwrite) throws IOException {
        boolean modified = false;

        for (OutboundRequest request : requests) {
            modified |= putRequestInternal(request, overwrite);
        }

        if (modified) {
            sortRequests();
            tryPersistConfig();
        }
    }

    private boolean putRequestInternal(@Nonnull OutboundRequest request, boolean overwrite) {
        Objects.requireNonNull(request, REQUEST_COULD_NOT_BE_NULL);
        OutboundRequest existing = findRequest(request.getId()).orElse(null);
        if (existing == null) {
            config.getRequests().add(request);
            notifyRequestCreated(request);
            return true;
        } else if (overwrite) {
            notifyRequestDeleted(existing);
            existing.assignFrom(request);
            notifyRequestCreated(existing);
            return true;
        }
        return false;
    }

    @Override
    public void deleteRequests(List<OutboundRequest> requests) throws IOException {
        boolean modified = false;

        for (OutboundRequest request : requests) {
            if (config.getRequests().remove(request)) {
                notifyRequestDeleted(request);
                modified = true;
            }
        }

        if (modified) {
            tryPersistConfig();
        }
    }

    private void notifyRequestCreated(OutboundRequest request) {
        requestObservers.forEach(o -> o.onRequestCreated(request));
    }

    private void notifyRequestDeleted(OutboundRequest request) {
        requestObservers.forEach(o -> o.onRequestDeleted(request));
    }

    //----------------------------------------------------------------------
    //
    //   Tests
    //
    //----------------------------------------------------------------------

    @Override
    public List<ApiTest> findAllTests() {
        return config.getTests();
    }

    @Override
    public Optional<ApiTest> findTest(@Nullable String alias) {
        if (alias == null) {
            return Optional.empty();
        }
        return findAllTests().stream()
                .filter(e -> alias.trim().equals(e.getAlias()))
                .findFirst();
    }

    @SuppressWarnings("java:S125")
    @Override
    public void putTest(@Nullable ApiTest existing, @Nonnull ApiTest apiTest) throws IOException {
        Objects.requireNonNull(apiTest, TEST_COULD_NOT_BE_NULL);
        existing = findTest(existing == null ? null : existing.getAlias()).orElse(null);
        if (existing == null) {
            ensureUniqueTestAlias(null, apiTest);
            config.getTests().add(apiTest);
            //notifyTestCreated(test);
        } else {
            ensureUniqueTestAlias(existing.getAlias(), apiTest);
            //notifyTestDeleted(existing);
            existing.assignFrom(apiTest);
            //notifyTestCreated(existing);
        }
        sortTests();
        tryPersistConfig();
    }

    private void ensureUniqueTestAlias(@Nullable String previousAlias, @Nonnull ApiTest apiTest) {
        Objects.requireNonNull(apiTest, TEST_COULD_NOT_BE_NULL);
        if (previousAlias != null && previousAlias.equals(apiTest.getAlias())) return;

        if (apiTest.getAlias() != null
                && !apiTest.getAlias().isBlank()
                && findTest(apiTest.getAlias()).isEmpty()) return;

        throw new TestAlreadyExistsException(apiTest);
    }

    @Override
    public void putTests(List<ApiTest> apiTests, boolean overwrite) throws IOException {
        boolean modified = false;
        for (ApiTest apiTest : apiTests) {
            modified |= putTestInternal(apiTest, overwrite);
        }
        if (modified) {
            sortTests();
            tryPersistConfig();
        }
    }

    @SuppressWarnings("java:S125")
    private boolean putTestInternal(@Nonnull ApiTest apiTest, boolean overwrite) {
        Objects.requireNonNull(apiTest, TEST_COULD_NOT_BE_NULL);
        ApiTest existing = findTest(apiTest.getAlias()).orElse(null);

        if (existing == null) {
            config.getTests().add(apiTest);
            //notifyTestCreated(test);
            return true;
        } else if (overwrite) {
            //notifyTestDeleted(existing);
            existing.assignFrom(apiTest);
            //notifyTestCreated(existing);
            return true;
        }
        return false;
    }

    @SuppressWarnings("java:S125")
    @Override
    public void deleteTests(List<ApiTest> apiTests) throws IOException {
        boolean modified = false;

        for (ApiTest apiTest : apiTests) {
            if (config.getTests().remove(apiTest)) {
                //notifyTestDeleted(test);
                modified = true;
            }
        }

        if (modified) {
            tryPersistConfig();
        }
    }

    //----------------------------------------------------------------------
    //
    //   Kafka
    //
    //----------------------------------------------------------------------

    @Override
    public List<KafkaTopic> findAllKafkaTopics() {
        return config.getKafkaTopics();
    }

    @Override
    public Optional<KafkaTopic> findKafkaTopic(@Nonnull String topic, int partition) {
        return findAllKafkaTopics().stream()
                .filter(e -> e.getTopic().equals(topic) && e.getPartition() == partition)
                .findFirst();
    }

    @Override
    public void putKafkaTopic(@Nullable KafkaTopic originalKafkaTopic, @Nonnull KafkaTopic kafkaTopic) throws IOException {
        Objects.requireNonNull(kafkaTopic, "Kafka topic could not be null.");
        putKafkaTopicToConfig(originalKafkaTopic, kafkaTopic);
        sortKafkaTopics();
        tryPersistConfig();
    }

    private void putKafkaTopicToConfig(@Nullable KafkaTopic reference, KafkaTopic kafkaTopic) {
        KafkaTopic existing = null;
        if (reference != null) existing = findKafkaTopic(reference.getTopic(), reference.getPartition()).orElse(null);

        if (existing == null) {
            putKafkaTopicNew(kafkaTopic);
        } else {
            putKafkaTopicExisting(kafkaTopic, existing);
        }
    }

    private void putKafkaTopicNew(KafkaTopic kafkaTopic) {
        KafkaTopic existing = findKafkaTopic(kafkaTopic.getTopic(), kafkaTopic.getPartition()).orElse(null);
        if (existing != null) {
            throw new KafkaTopicAlreadyExistsException(kafkaTopic);
        }
        config.getKafkaTopics().add(kafkaTopic);
        //notifyKafkaTopicCreated(kafkaTopic);
    }

    private void putKafkaTopicExisting(KafkaTopic kafkaTopic, KafkaTopic existingOriginal) {
        KafkaTopic existingKafkaTopic = findKafkaTopic(kafkaTopic.getTopic(), kafkaTopic.getPartition()).orElse(null);
        if (existingKafkaTopic != null && !existingOriginal.equals(existingKafkaTopic)) {
            throw new KafkaTopicAlreadyExistsException(kafkaTopic);
        }
        //notifyKafkaTopicDeleted(existingOriginal);
        existingOriginal.assignFrom(kafkaTopic);
        //notifyKafkaTopicCreated(existingOriginal);
    }

    @Override
    public void putKafkaTopics(List<KafkaTopic> kafkaTopics, boolean overwrite) throws IOException {
        boolean modified = false;

        for (KafkaTopic kafkaTopic : kafkaTopics) {
            modified |= putKafkaTopicInternal(kafkaTopic, overwrite);
        }

        if (modified) {
            sortKafkaTopics();
            tryPersistConfig();
        }
    }

    private boolean putKafkaTopicInternal(@Nonnull KafkaTopic topic, boolean overwrite) {
        Objects.requireNonNull(topic, "Kafka topic could not be null.");
        KafkaTopic existing = findKafkaTopic(topic.getTopic(), topic.getPartition()).orElse(null);

        if (existing == null) {
            config.getKafkaTopics().add(topic);
            //notifyKafkaTopicCreated(topic);
            return true;
        } else if (overwrite) {
            //notifyKafkaTopicDeleted(existing);
            existing.assignFrom(topic);
            //notifyKafkaTopicCreated(existing);
            return true;
        }
        return false;
    }

    @Override
    public void deleteKafkaTopics(List<KafkaTopic> topics) throws IOException {
        boolean modified = false;

        for (KafkaTopic kafkaTopic : topics) {
            if (config.getKafkaTopics().remove(kafkaTopic)) {
                //notifyKafkaTopicDeleted(kafkaTopic);
                modified = true;
            }
        }

        if (modified) {
            tryPersistConfig();
        }
    }
}
