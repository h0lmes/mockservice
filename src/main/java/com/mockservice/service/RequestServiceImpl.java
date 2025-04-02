package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.config.WebClientFactory;
import com.mockservice.domain.OutboundRequest;
import com.mockservice.exception.RequestServiceRequestException;
import com.mockservice.mapper.OutboundRequestMapper;
import com.mockservice.model.OutboundRequestDto;
import com.mockservice.model.OutboundRequestResult;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.template.MockVariables;
import com.mockservice.template.RequestHeadersTemplate;
import com.mockservice.template.StringTemplate;
import com.mockservice.template.TemplateEngine;
import com.mockservice.util.MapUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class RequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceImpl.class);

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private static final int MAX_LEVEL_FOR_TRIGGERED_REQUEST = 3;

    private final WebClientFactory webClientFactory;
    private final ConfigRepository configRepository;
    private final OutboundRequestMapper requestMapper;
    private final TemplateEngine templateEngine;
    private final VariablesService variablesService;
    private final ObjectMapper jsonMapper;
    private final ConcurrentLruCache<OutboundRequest, RequestHeadersTemplate> headersCache;
    private final ConcurrentLruCache<OutboundRequest, StringTemplate> uriCache;
    private final ConcurrentLruCache<OutboundRequest, StringTemplate> requestBodyCache;

    public RequestServiceImpl(WebClientFactory webClientFactory,
                              ConfigRepository configRepository,
                              OutboundRequestMapper requestMapper,
                              TemplateEngine templateEngine,
                              VariablesService variablesService,
                              @Value("${application.request-service.cache-size}") int cacheSize,
                              @Qualifier("jsonMapper") ObjectMapper jsonMapper) {
        this.webClientFactory = webClientFactory;
        this.configRepository = configRepository;
        this.requestMapper = requestMapper;
        this.templateEngine = templateEngine;
        this.variablesService = variablesService;
        this.jsonMapper = jsonMapper;
        headersCache = new ConcurrentLruCache<>(cacheSize, this::getRequestHeadersTemplate);
        uriCache = new ConcurrentLruCache<>(cacheSize, this::getRequestUriTemplate);
        requestBodyCache = new ConcurrentLruCache<>(cacheSize, this::getRequestBodyTemplate);
    }

    private RequestHeadersTemplate getRequestHeadersTemplate(OutboundRequest request) {
        return new RequestHeadersTemplate(request.getHeaders());
    }

    private StringTemplate getRequestUriTemplate(OutboundRequest request) {
        return new StringTemplate(
                request.getPath().contains("://") ? request.getPath() : "http://" + request.getPath());
    }

    private StringTemplate getRequestBodyTemplate(OutboundRequest request) {
        return new StringTemplate(request.getBody());
    }

    @Override
    public void schedule(String requestIds, @Nullable MockVariables variables) {
        scheduleRequests(requestIds, variables, 0);
    }

    @Override
    public Optional<OutboundRequestResult> executeRequest(String requestId,
                                           @Nullable MockVariables variables,
                                           boolean allowTrigger) {
        return executeRequestById(requestId, variables, 0, allowTrigger);
    }

    private void scheduleRequests(String requestIds,
                                  @Nullable MockVariables variables, int level) {
        if (Strings.isBlank(requestIds)) return;
        CompletableFuture.runAsync(() -> executeRequests(requestIds, variables, level));
    }

    private void executeRequests(String requestIds,
                                 @Nullable MockVariables variables,
                                 int level) {
        for (String requestId : requestIds.split(",")) {
            executeRequestById(requestId, variables, level, true);
        }
    }

    private Optional<OutboundRequestResult> executeRequestById(String requestId,
                                                @Nullable MockVariables variables,
                                                int level, boolean allowTrigger) {
        return getEnabledRequest(requestId)
                .flatMap(r -> executeRequest(r, variables, level, allowTrigger));
    }

    private Optional<OutboundRequestResult> executeRequest(OutboundRequest request,
                                                           @Nullable MockVariables inVariables,
                                                           int level,
                                                           boolean allowTrigger) {
        if (level > MAX_LEVEL_FOR_TRIGGERED_REQUEST) {
            // try preventing infinite loop of requests
            log.info("Max level for triggered request reached for: {}", request);
            return Optional.empty();
        }

        MockVariables requestVars = MockVariables.sum(variablesService.getAll(), inVariables);
        MockVariables responseVars = null;
        var requestBody = requestBodyCache.get(request)
                .toString(requestVars, templateEngine.getFunctions());
        String uri = uriCache.get(request)
                .toString(requestVars, templateEngine.getFunctions());
        var headersTemplate = headersCache.get(request);
        var headers = headersTemplate
                .toMap(requestVars, templateEngine.getFunctions());
        var startMillis = System.currentTimeMillis();

        try {
            log.info("Executing request: {}", request.getId());
            String body = webClientFactory
                    .create(uri.startsWith("https://"))
                    .method(request.getMethod().asHttpMethod())
                    .uri(uri)
                    .bodyValue(requestBody)
                    .headers(c -> c.putAll(headers))
                    .retrieve()
                    // wrap any response into exception to extract both StatusCode and Body
                    .onStatus(code -> true, res -> res.bodyToMono(String.class)
                            .handle((error, sink) -> sink.error(
                                    new RequestServiceRequestException(res.statusCode().value(), error)
                            )
                    ))
                    .bodyToMono(String.class)
                    .block(REQUEST_TIMEOUT);

            throw new RequestServiceRequestException(200, body); // just in case
        } catch (RequestServiceRequestException e) {
            log.info("Request ({}) response:\n{}", request.getId(), e.toString());
            responseVars = jsonToMap(e.getBody());
            return Optional.of(
                    new OutboundRequestResult(false, request, uri,
                    headersTemplate, requestBody, e.getBody(), responseVars, e.getCode(), startMillis));
        } catch (Exception e) {
            log.error("Request (" + request.getId() + ") error.", e);
            return Optional.of(
                    new OutboundRequestResult(true, request, uri,
                    headersTemplate, requestBody, e.getMessage(), MockVariables.empty(), 0, startMillis));
        } finally {
            if (request.isResponseToVars()) {
                variablesService.putAll(responseVars);
                variablesService.putAll(request.getGroup(), responseVars);
            }
            if (allowTrigger && request.isTriggerRequest()) {
                scheduleRequests(request.getTriggerRequestIds(), responseVars, level + 1);
            }
        }
    }

    private MockVariables jsonToMap(String json) {
        try {
            return MockVariables.of(
                    MapUtils.flattenMap(
                            MapUtils.jsonToMap(json, jsonMapper)));
        } catch (JsonProcessingException jpe) {
            log.warn("Request; invalid JSON:\n{}", jpe.getMessage());
            return MockVariables.empty();
        }
    }

    @Override
    public Optional<OutboundRequest> getEnabledRequest(String requestId) {
        return configRepository.findRequest(requestId).filter(r -> !r.isDisabled());
    }

    @Override
    public List<OutboundRequestDto> getRequests() {
        return requestMapper.toDto(configRepository.findAllRequests());
    }

    @Override
    public void putRequest(OutboundRequestDto existing, OutboundRequestDto request) throws IOException {
        var ex = requestMapper.fromDto(existing);
        cacheRemove(ex);
        configRepository.putRequest(ex, requestMapper.fromDto(request));
    }

    @Override
    public void putRequests(List<OutboundRequestDto> dto, boolean overwrite) throws IOException {
        var requests = requestMapper.fromDto(dto);
        requests.forEach(this::cacheRemove);
        configRepository.putRequests(requests, overwrite);
    }

    @Override
    public void deleteRequests(List<OutboundRequestDto> dto) throws IOException {
        var requests = requestMapper.fromDto(dto);
        requests.forEach(this::cacheRemove);
        configRepository.deleteRequests(requests);
    }

    private void cacheRemove(OutboundRequest r) {
        headersCache.remove(r);
        uriCache.remove(r);
        requestBodyCache.remove(r);
    }
}
