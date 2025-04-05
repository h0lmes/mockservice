package com.mockservice.service;

import com.mockservice.domain.OutboundRequest;
import com.mockservice.mapper.OutboundRequestMapper;
import com.mockservice.model.HttpRequestResult;
import com.mockservice.model.OutboundRequestDto;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.template.MockVariables;
import com.mockservice.template.RequestHeadersTemplate;
import com.mockservice.template.StringTemplate;
import com.mockservice.template.TemplateEngine;
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

@Service
public class RequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceImpl.class);
    private static final int MAX_LEVEL_FOR_TRIGGERED_REQUEST = 3;

    private final ConfigRepository configRepository;
    private final OutboundRequestMapper requestMapper;
    private final TemplateEngine templateEngine;
    private final VariablesService variablesService;
    private final HttpService httpService;
    private final ConcurrentLruCache<OutboundRequest, RequestHeadersTemplate> headersCache;
    private final ConcurrentLruCache<OutboundRequest, StringTemplate> uriCache;
    private final ConcurrentLruCache<OutboundRequest, StringTemplate> requestBodyCache;

    public RequestServiceImpl(@Value("${application.request-service.cache-size:1000}") int cacheSize,
                              ConfigRepository configRepository,
                              OutboundRequestMapper requestMapper,
                              TemplateEngine templateEngine,
                              VariablesService variablesService,
                              HttpService httpService) {
        this.configRepository = configRepository;
        this.requestMapper = requestMapper;
        this.templateEngine = templateEngine;
        this.variablesService = variablesService;
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
    public void schedule(String requestIds, @Nullable MockVariables variables) {
        scheduleRequests(requestIds, variables, 0);
    }

    @Override
    public Optional<HttpRequestResult> executeRequest(String requestId,
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

        var requestVars = MockVariables.sum(variablesService.getAll(), inVariables);
        var requestBody = requestBodyCache.get(request).toString(requestVars, templateEngine.getFunctions());
        var uri = uriCache.get(request).toString(requestVars, templateEngine.getFunctions());
        var headersTemplate = headersCache.get(request);
        var headers = headersTemplate.toMap(requestVars, templateEngine.getFunctions());

        var result = httpService.request(request.getMethod(), uri, requestBody, headers);

        result.ifPresent(res -> {
            if (request.isResponseToVars()) {
                variablesService.putAll(res.getResponseVariables());
                variablesService.putAll(request.getGroup(), res.getResponseVariables());
            }
            if (allowTrigger && request.isTriggerRequest()) {
                scheduleRequests(request.getTriggerRequestIds(), res.getResponseVariables(), level + 1);
            }
        });

        return result;
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
