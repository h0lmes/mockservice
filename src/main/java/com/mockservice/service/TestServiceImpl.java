package com.mockservice.service;

import com.mockservice.domain.ApiTest;
import com.mockservice.mapper.ApiTestMapper;
import com.mockservice.model.ApiTestDto;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.template.StringTemplate;
import com.mockservice.template.TemplateEngine;
import com.mockservice.util.IOUtils;
import com.mockservice.ws.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class TestServiceImpl implements TestService {
    private static final Logger log = LoggerFactory.getLogger(TestServiceImpl.class);

    private static final long EXECUTION_DELAY_MILLISECONDS = 100;
    private static final String VAR_DELIMITER = "=";
    private static final int VAR_DELIMITER_LEN = VAR_DELIMITER.length();
    private static final String PROCESSING_VAR = "Processing variable: ";
    private static final String PROCESSING_REQUEST = "Processing request: ";
    private static final String RESULT = "Result: ";
    private final ConfigRepository configRepository;
    private final ApiTestMapper apiTestMapper;
    private final TemplateEngine templateEngine;
    private final VariablesService variablesService;
    private final RequestService requestService;
    private final WebSocketHandler webSocketHandler;
    private final Map<ApiTest, TestRun> runs = new ConcurrentHashMap<>();

    public TestServiceImpl(
            ConfigRepository configRepository,
            ApiTestMapper apiTestMapper,
            TemplateEngine templateEngine,
            VariablesService variablesService,
            RequestService requestService,
            WebSocketHandler webSocketHandler) {
        this.configRepository = configRepository;
        this.apiTestMapper = apiTestMapper;
        this.templateEngine = templateEngine;
        this.variablesService = variablesService;
        this.requestService = requestService;
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * backlog
     * - run tests async
     * - only one instance of each test at any moment
     * - can stop test
     * - can see test progress percent and log
     */

    @Override
    public TestRunStatus executeTest(String alias, boolean allowTrigger) {
        ApiTest apiTest = getEnabledTest(alias).orElse(null);
        if (apiTest == null) return TestRunStatus.NOT_FOUND;

        TestRun run = runs.computeIfAbsent(apiTest, t -> new TestRun());
        if (!run.init(apiTest, allowTrigger)) return TestRunStatus.ALREADY_IN_PROGRESS;

        CompletableFuture.runAsync(() -> execute(run),
                CompletableFuture.delayedExecutor(EXECUTION_DELAY_MILLISECONDS, TimeUnit.MILLISECONDS));
        return TestRunStatus.OK;
    }

    @Override
    public TestRunStatus stopTest(String alias) {
        ApiTest apiTest = getEnabledTest(alias).orElse(null);
        if (apiTest == null) return TestRunStatus.NOT_FOUND;
        TestRun run = runs.get(apiTest);
        if (run == null) return TestRunStatus.NOT_FOUND;
        run.requestStop();
        return TestRunStatus.OK;
    }

    @Override
    public String getTestResult(String alias) {
        ApiTest apiTest = getEnabledTest(alias).orElse(null);
        if (apiTest == null) return "Test not found";
        TestRun run = runs.get(apiTest);
        if (run == null) return "Test not run yet";
        if (run.isEmpty()) return "Empty log";
        return run.getLog();
    }

    @Override
    public TestRunStatus clearTestResult(String alias) {
        ApiTest apiTest = getEnabledTest(alias).orElse(null);
        if (apiTest == null) return TestRunStatus.NOT_FOUND;
        TestRun run = runs.get(apiTest);
        if (run == null) return TestRunStatus.NOT_FOUND;
        if (!run.clear()) return TestRunStatus.ALREADY_IN_PROGRESS;
        return TestRunStatus.OK;
    }

    @SuppressWarnings("java:S1149")
    private void execute(TestRun run) {
        ApiTest apiTest = run.getTest();
        boolean allowTrigger = run.isAllowTrigger();

        var startedAt = Instant.now();
        run.log("START test [")
                .log(apiTest.getAlias())
                .log("] at ")
                .log(startedAt.toString())
                .log('\n');
        log.info("START test: {}", apiTest.getAlias());
        notifyExecutionProgress(run);

        List<String> lines = IOUtils.toList(apiTest.getPlan());
        for (String line : lines) {
            if (run.isRequireStop()) {
                run.setRunning(false);
                run.log('\n')
                        .log("Test [")
                        .log(apiTest.getAlias())
                        .log("] stopped by user");
                log.info("Test stopped by user: {}", apiTest.getAlias());
                notifyExecutionProgress(run);
                return;
            }
            if (!line.trim().isEmpty()) {
                logTestStepStart(run);
                executeLine(run, line, allowTrigger);
                notifyExecutionProgress(run);
            }
        }

        run.setRunning(false);
        run.log('\n')
                .log("END test [")
                .log(apiTest.getAlias())
                .log("] in ")
                .log(Instant.now().toEpochMilli() - startedAt.toEpochMilli())
                .log(" milliseconds");
        log.info("END test: {}", apiTest.getAlias());
        notifyExecutionProgress(run);
    }

    @SuppressWarnings("java:S1149")
    private void logTestStepStart(TestRun run) {
        run.setCurrentLine(run.getCurrentLine() + 1);
        run.log('\n')
                .log("STEP ")
                .log(run.getCurrentLine())
                .log('\n');
    }

    private void notifyExecutionProgress(TestRun run) {
        webSocketHandler.broadcastTestResult(
                run.getTest().getAlias(),
                run.getLog());
    }

    @SuppressWarnings("java:S1149")
    private void executeLine(TestRun run, String line, boolean allowTrigger) {
        if (line.contains(VAR_DELIMITER)) {
            executeVar(run, line);
            return;
        }
        executeRequest(run, line, allowTrigger);
    }

    @SuppressWarnings("java:S1149")
    private void executeVar(TestRun run, String line) {
        run.log(PROCESSING_VAR).log(line).log('\n');

        try {
            int delimiter = line.indexOf(VAR_DELIMITER);
            String key = line.substring(0, delimiter).trim();
            String value = line.substring(delimiter + VAR_DELIMITER_LEN).trim();
            StringTemplate valueTemplate = new StringTemplate(value);
            value = valueTemplate.toString(variablesService.getAll(), templateEngine.getFunctions());
            variablesService.put(key, value);

            run.log(RESULT).log(key).log(VAR_DELIMITER).log(value).log('\n');
        } catch (Exception e) {
            log.error("ERROR while processing variable. ", e);
            run.log("ERROR while processing variable.")
                    .log('\n')
                    .log(e.toString())
                    .log('\n');
        }
    }

    @SuppressWarnings("java:S1149")
    private void executeRequest(TestRun run, String line, boolean allowTrigger) {
        run.log(PROCESSING_REQUEST).log(line).log('\n');
        try {
            String result = requestService.executeRequest(line, null, allowTrigger)
                    .orElse("...nothing...");
            run.log(result).log('\n');
        } catch (Exception e) {
            log.error("ERROR while executing request. ", e);
            run.log("ERROR while executing request.")
                    .log('\n')
                    .log(e.toString())
                    .log('\n');
        }
    }

    @Override
    public Optional<ApiTest> getEnabledTest(String alias) {
        return configRepository.findTest(alias);
    }

    @Override
    public List<ApiTestDto> getTests() {
        return apiTestMapper.toDto(configRepository.findAllTests());
    }

    @Override
    public void putTest(ApiTestDto existing, ApiTestDto test) throws IOException {
        configRepository.putTest(apiTestMapper.fromDto(existing), apiTestMapper.fromDto(test));
    }

    @Override
    public void putTests(List<ApiTestDto> dto, boolean overwrite) throws IOException {
        configRepository.putTests(apiTestMapper.fromDto(dto), overwrite);
    }

    @Override
    public void deleteTests(List<ApiTestDto> dto) throws IOException {
        configRepository.deleteTests(apiTestMapper.fromDto(dto));
    }
}
