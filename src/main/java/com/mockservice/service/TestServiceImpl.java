package com.mockservice.service;

import com.mockservice.domain.ApiTest;
import com.mockservice.mapper.ApiTestMapper;
import com.mockservice.model.ApiTestDto;
import com.mockservice.model.HttpRequestResult;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.template.MockFunctions;
import com.mockservice.template.MockVariables;
import com.mockservice.template.StringTemplate;
import com.mockservice.util.KeyValue;
import com.mockservice.ws.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TestServiceImpl implements TestService {
    public static final String SUCCESS = "SUCCESS";
    public static final String WARNING = "WARNING";
    public static final String FAILED = "FAILED";
    public static final String ERROR = "ERROR (";
    private static final Logger log = LoggerFactory.getLogger(TestServiceImpl.class);
    private final ConfigRepository configRepository;
    private final ApiTestMapper apiTestMapper;
    private final VariablesService variablesService;
    private final RequestService requestService;
    private final HttpService httpService;
    private final WebSocketHandler webSocketHandler;
    private final Map<ApiTest, TestRun> runs = new ConcurrentHashMap<>();
    private final ConcurrentLruCache<String, StringTemplate> templateCache;

    public TestServiceImpl(
            @Value("${application.test-service.cache-size:2000}") int cacheSize,
            ConfigRepository configRepository,
            ApiTestMapper apiTestMapper,
            VariablesService variablesService,
            RequestService requestService,
            HttpService httpService,
            WebSocketHandler webSocketHandler) {
        this.configRepository = configRepository;
        this.apiTestMapper = apiTestMapper;
        this.variablesService = variablesService;
        this.requestService = requestService;
        this.httpService = httpService;
        this.webSocketHandler = webSocketHandler;
        templateCache = new ConcurrentLruCache<>(cacheSize, StringTemplate::new);
    }

    @Override
    public TestRunStatus execute(String alias, boolean allowTrigger, boolean async) {
        ApiTest apiTest = getEnabledTest(alias).orElse(null);
        if (apiTest == null) return TestRunStatus.NOT_FOUND;

        TestRun run = runs.computeIfAbsent(apiTest, t -> new TestRun());
        if (!run.init(apiTest, allowTrigger)) return TestRunStatus.ALREADY_IN_PROGRESS;

        if (async) CompletableFuture.runAsync(() -> execute(run));
        else execute(run);

        return TestRunStatus.OK;
    }

    @Override
    public TestRunStatus stop(String alias) {
        ApiTest apiTest = getEnabledTest(alias).orElse(null);
        if (apiTest == null) return TestRunStatus.NOT_FOUND;

        TestRun run = runs.get(apiTest);
        if (run == null) return TestRunStatus.NOT_FOUND;

        run.requestStop();
        return TestRunStatus.OK;
    }

    @Override
    public String getTestLog(String alias) {
        ApiTest apiTest = getEnabledTest(alias).orElse(null);
        if (apiTest == null) return "Test not found";

        TestRun run = runs.get(apiTest);
        if (run == null) return "Test not run yet";

        if (run.isEmpty()) return "Empty log";
        return run.getLog();
    }

    @Override
    public TestRunStatus clearTestLog(String alias) {
        ApiTest apiTest = getEnabledTest(alias).orElse(null);
        if (apiTest == null) return TestRunStatus.NOT_FOUND;

        TestRun run = runs.get(apiTest);
        if (run == null) return TestRunStatus.NOT_FOUND;

        if (!run.clear()) return TestRunStatus.ALREADY_IN_PROGRESS;
        return TestRunStatus.OK;
    }

    private void execute(TestRun run) {
        logTestStart(run);
        notifyExecutionProgress(run);

        while (run.hasLine() && !run.isFailed()) {
            if (run.isRequestStop()) {
                run.setRunning(false);
                logTestStopped(run);
                notifyExecutionProgress(run);
                return;
            }

            logTestStepStart(run);
            executeLine(run);
            notifyExecutionProgress(run);
            run.nextLine();
        }

        run.setRunning(false);
        logTestEnd(run);
        notifyExecutionProgress(run);
    }

    private void logTestStart(TestRun run) {
        run.log("START test [").log(run.getTest().getAlias())
                .log("] at ").log(run.getStartedAt().toString()).log('\n');
        if (log.isInfoEnabled()) {
            log.info("START test: {}", run.getTest().getAlias());
        }
    }

    private void logTestStepStart(TestRun run) {
        String line = run.getLine();
        if (line.trim().isEmpty()) return;
        run.log("\nSTEP ").log(run.getStep()).log('\n');
    }

    private void logTestStopped(TestRun run) {
        run.log("\nTest [").log(run.getTest().getAlias()).log("] stopped by user");
        if (log.isInfoEnabled()) {
            log.info("Test [{}] stopped by user", run.getTest().getAlias());
        }
    }

    private void logTestEnd(TestRun run) {
        run.log("\nTEST ").log(run.getErrorLevel().name()).log(" in ")
                .log(run.getDurationMillis()).log(" milliseconds");
        if (log.isInfoEnabled()) {
            log.info("TEST {} in {} milliseconds",
                    run.getErrorLevel().name(), run.getDurationMillis());
        }
    }

    private void notifyExecutionProgress(TestRun run) {
        webSocketHandler.broadcastTestResult(run.getTest().getAlias(), run.getLog());
    }

    private void executeLine(TestRun run) {
        try {
            String line = run.getLine();
            if (line.trim().isEmpty()) return;

            if (line.startsWith("GET ") ||
                    line.startsWith("POST ") ||
                    line.startsWith("PUT ") ||
                    line.startsWith("PATCH ") ||
                    line.startsWith("DELETE ")) {
                executeInlineRequest(run);
                return;
            }
            if (line.contains("->")) {
                executeRequest(run);
                return;
            }
            if (line.contains("===")) {
                executeVarEqualsStrict(run);
                return;
            }
            if (line.contains("==")) {
                executeVarEquals(run);
                return;
            }
            if (line.contains("=")) {
                executeVarEvaluate(run);
                return;
            }
            executeRequest(run);
        } catch (Exception e) {
            log.error("ERROR executing line. ", e);
            run.log(ERROR).log("while executing line:\n").log(e.toString()).log(")\n");
            run.setErrorLevel(TestRunErrorLevel.FAILED);
        }
    }

    private void executeVarEquals(TestRun run) {
        try {
            var kv = KeyValue.of(run.getLine(), "==");
            String expectedValue = new StringTemplate(kv.value())
                    .toString(variablesService.getAll(), null);
            String value = MockVariables.get(run.getMockVariables(), kv.key());

            if (value.equals(expectedValue)) {
                run.log(SUCCESS).log(" (").log(kv.key()).log(" == ").log(value).log(")\n");
            } else {
                run.log(WARNING).log(" (").log(kv.key()).log(" == ").log(value)
                        .log("; expected: ").log(expectedValue).log(")\n");
                run.setErrorLevel(TestRunErrorLevel.WARNING);
            }
        } catch (Exception e) {
            log.error("ERROR comparing variable. ", e);
            run.log(ERROR).log("while comparing variable:\n").log(e.toString()).log(")\n");
            run.setErrorLevel(TestRunErrorLevel.FAILED);
        }
    }

    private void executeVarEqualsStrict(TestRun run) {
        try {
            var kv = KeyValue.of(run.getLine(), "===");
            String expectedValue = new StringTemplate(kv.value())
                    .toString(variablesService.getAll(), null);
            String value = MockVariables.get(run.getMockVariables(), kv.key());

            if (value.equals(expectedValue)) {
                run.log(SUCCESS).log(" (").log(kv.key()).log(" === ").log(value).log(")\n");
            } else {
                run.log(FAILED).log(" (").log(kv.key()).log(" === ").log(value)
                        .log("; expected: ").log(expectedValue).log(")\n");
                run.setErrorLevel(TestRunErrorLevel.FAILED);
            }
        } catch (Exception e) {
            log.error("ERROR comparing variable. ", e);
            run.log(ERROR).log("while comparing variable:\n").log(e.toString()).log(")\n");
            run.setErrorLevel(TestRunErrorLevel.FAILED);
        }
    }

    private void executeVarEvaluate(TestRun run) {
        try {
            var kv = KeyValue.of(run.getLine(), "=");
            StringTemplate valueTemplate = new StringTemplate(kv.value());
            String value = valueTemplate.toString(variablesService.getAll(), MockFunctions.create());

            variablesService.put(kv.key(), value);
            run.log(SUCCESS).log(" (").log(kv.key()).log(" = ").log(value).log(")\n");
        } catch (Exception e) {
            log.error("ERROR while processing variable. ", e);
            run.log(ERROR).log("while processing variable:\n").log(e.toString()).log(")\n");
            run.setErrorLevel(TestRunErrorLevel.FAILED);
        }
    }

    private void executeRequest(TestRun run) {
        try {
            var kvStep = KeyValue.of(run.getLine(), "->");
            String request = kvStep.key();
            String codes = kvStep.value();
            run.log("Request: ").log(request).log('\n');

            Optional<HttpRequestResult> requestResult = requestService.executeRequest(
                    request, null, run.isAllowTrigger());

            processRequestResult(run, requestResult, codes);
        } catch (Exception e) {
            log.error("ERROR while executing request. ", e);
            run.log(ERROR).log("while executing request:\n").log(e.toString()).log(")\n");
            run.setErrorLevel(TestRunErrorLevel.FAILED);
        }
    }

    private void executeInlineRequest(TestRun run) {
        try {
            var kvStep = KeyValue.of(run.getLine(), "->");
            String request = kvStep.key();
            String codes = kvStep.value();
            run.log("Request: ").log(request).log('\n');

            var kvRequest = KeyValue.of(request, " ");
            var kvUriBody = KeyValue.of(kvRequest.value(), " ");
            RequestMethod method = RequestMethod.resolve(kvRequest.key());
            var functions = MockFunctions.create();
            String uri = templateCache.get(kvUriBody.key())
                    .toString(variablesService.getAll(), functions);
            String body = templateCache.get(kvUriBody.value())
                    .toString(variablesService.getAll(), functions);

            Optional<HttpRequestResult> requestResult = httpService.request(
                    method, uri, body, null);

            processRequestResult(run, requestResult, codes);
        } catch (Exception e) {
            log.error("ERROR while executing request. ", e);
            run.log(ERROR).log("while executing request:\n").log(e.toString()).log(")\n");
            run.setErrorLevel(TestRunErrorLevel.FAILED);
        }
    }

    private void processRequestResult(TestRun run,
                                      Optional<HttpRequestResult> requestResult,
                                      String codes) {
        String result = requestResult.map(Objects::toString).orElse("...nothing...");
        run.log(result).log('\n');

        requestResult.ifPresent(res -> {
            run.setMockVariables(res.getResponseVariables());
            if (codes == null || codes.isEmpty() || codes.contains("" + res.getStatusCode())) {
                run.log("SUCCESS\n");
            } else {
                run.log(FAILED).log(" (").log("status code = ").log(res.getStatusCode())
                        .log("; expected: ").log(codes).log(")\n");
                run.setErrorLevel(TestRunErrorLevel.FAILED);
            }
        });
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
