package com.mockachu.service;

import com.mockachu.domain.OutboundRequest;
import com.mockachu.mapper.OutboundRequestMapper;
import com.mockachu.model.HttpRequestResult;
import com.mockachu.model.OutboundRequestDto;
import com.mockachu.repository.ConfigRepository;
import com.mockachu.template.MockFunctions;
import com.mockachu.template.MockVariables;
import com.mockachu.template.RequestHeadersTemplate;
import com.mockachu.template.StringTemplate;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class RequestServiceImpl implements RequestService {
    private static final Logger log = LoggerFactory.getLogger(RequestServiceImpl.class);
    public static final int MAX_LEVEL_FOR_TRIGGERED_REQUEST = 5;

    private final ConfigRepository configRepository;
    private final OutboundRequestMapper requestMapper;
    private final ContextService contextService;
    private final HttpService httpService;
    private final ConcurrentLruCache<OutboundRequest, RequestHeadersTemplate> headersCache;
    private final ConcurrentLruCache<OutboundRequest, StringTemplate> uriCache;
    private final ConcurrentLruCache<OutboundRequest, StringTemplate> requestBodyCache;

    public RequestServiceImpl(@Value("${application.request-service.cache-size:1000}") int cacheSize,
                              ConfigRepository configRepository,
                              OutboundRequestMapper requestMapper,
                              ContextService contextService,
                              HttpService httpService) {
        this.configRepository = configRepository;
        this.requestMapper = requestMapper;
        this.contextService = contextService;
        this.httpService = httpService;
        headersCache = new ConcurrentLruCache<>(cacheSize, this::getRequestHeadersTemplate);
        uriCache = new ConcurrentLruCache<>(cacheSize, this::getRequestUriTemplate);
        requestBodyCache = new ConcurrentLruCache<>(cacheSize, this::getRequestBodyTemplate);
    }

    private RequestHeadersTemplate getRequestHeadersTemplate(OutboundRequest request) {
        return new RequestHeadersTemplate(request.getHeaders());
    }

    private StringTemplate getRequestUriTemplate(OutboundRequest request) {
        return new StringTemplate(request.getPath());
    }

    private StringTemplate getRequestBodyTemplate(OutboundRequest request) {
        return new StringTemplate(request.getBody());
    }

    @Override
    public void schedule(String requestIds, String requestDelays, @Nullable MockVariables variables) {
        scheduleRequests(requestIds, requestDelays, variables, 0);
    }

    @Override
    public Optional<HttpRequestResult> executeRequest(String requestId,
                                                      @Nullable MockVariables variables,
                                                      boolean allowTrigger) {
        return executeRequestById(requestId, variables, 0, allowTrigger);
    }

    private void scheduleRequests(String requestIds, String requestDelays,
                                  @Nullable MockVariables variables, int level) {
        if (Strings.isBlank(requestIds)) return;

        var idArray = requestIds.split(",");
        var delayArray = requestDelays.split(",");
        for (int i = 0; i < idArray.length; i++) {
            long delay = 100;
            if (i < delayArray.length) {
                try {
                    delay = Long.parseLong(delayArray[i].trim());
                } catch (Exception e) {
                    // don't care
                }
            }
            scheduleRequest(idArray[i].trim(), delay, variables, level);
        }
    }

    private void scheduleRequest(String requestId, long delay,
                                 @Nullable MockVariables variables, int level) {
        if (delay == 0) {
            CompletableFuture.runAsync(
                    () -> executeRequestById(requestId, variables, level, true));
        } else {
            CompletableFuture.runAsync(
                    () -> executeRequestById(requestId, variables, level, true),
                    CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS));
        }
    }

    private Optional<HttpRequestResult> executeRequestById(String requestId,
                                                           @Nullable MockVariables variables,
                                                           int level, boolean allowTrigger) {
        return getEnabledRequest(requestId)
                .flatMap(r -> executeRequest(r, variables, level, allowTrigger));
    }

    private Optional<HttpRequestResult> executeRequest(OutboundRequest request,
                                                       @Nullable MockVariables inVariables,
                                                       int level,
                                                       boolean allowTrigger) {
        if (level > MAX_LEVEL_FOR_TRIGGERED_REQUEST) {
            // try preventing infinite loop of requests
            log.info("Max level for triggered request reached for: {}", request);
            return Optional.empty();
        }

        var functions = MockFunctions.create();
        var requestVars = MockVariables.sum(contextService.get(), inVariables);

        var requestBody = requestBodyCache.get(request).toString(requestVars, functions);
        var uri = uriCache.get(request).toString(requestVars, functions);
        var headers = headersCache.get(request).toMap(requestVars, functions);

        var result = httpService.request(
                request.getMethod(), uri, requestBody, headers);

        if (result != null) {
            if (request.isResponseToVars()) {
                contextService.putAll(result.getResponseVariables());
                contextService.putAll(request.getGroup(), result.getResponseVariables());
            }
            if (allowTrigger && request.isTriggerRequest()) {
                scheduleRequests(request.getTriggerRequestIds(),
                        request.getTriggerRequestDelay(),
                        result.getResponseVariables(),
                        level + 1);
            }
        }

        return Optional.ofNullable(result);
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
    public synchronized void putRequest(OutboundRequestDto existing, OutboundRequestDto request) throws IOException {
        var ex = requestMapper.fromDto(existing);
        cacheRemove(ex);
        configRepository.putRequest(ex, requestMapper.fromDto(request));
    }

    @Override
    public synchronized void putRequests(List<OutboundRequestDto> dto, boolean overwrite) throws IOException {
        var requests = requestMapper.fromDto(dto);
        requests.forEach(this::cacheRemove);
        configRepository.putRequests(requests, overwrite);
    }

    @Override
    public synchronized void deleteRequests(List<OutboundRequestDto> dto) throws IOException {
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
