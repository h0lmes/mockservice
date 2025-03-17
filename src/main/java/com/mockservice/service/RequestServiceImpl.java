package com.mockservice.service;

import com.mockservice.domain.OutboundRequest;
import com.mockservice.mapper.OutboundRequestMapper;
import com.mockservice.model.OutboundRequestDto;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.response.MockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class RequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceImpl.class);

    private static final long EXECUTION_DELAY_SECONDS = 2;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final ConfigRepository configRepository;
    private final OutboundRequestMapper requestMapper;

    public RequestServiceImpl(ConfigRepository configRepository, OutboundRequestMapper requestMapper) {
        this.configRepository = configRepository;
        this.requestMapper = requestMapper;
    }

    @Override
    public void schedule(MockResponse response) {
        CompletableFuture.runAsync(
                () -> executeRequest(response),
                CompletableFuture.delayedExecutor(EXECUTION_DELAY_SECONDS, TimeUnit.SECONDS)
        );
    }

    private void executeRequest(MockResponse mockResponse) {
        try {
            String url = mockResponse.getRequestUrl();

            log.info("Callback request: {}, {}, {}, {}",
                    mockResponse.getRequestMethod(),
                    url,
                    mockResponse.getRequestBody(),
                    mockResponse.getRequestHeaders());

            String response = WebClient.create()
                    .method(mockResponse.getRequestMethod())
                    .uri(url)
                    .bodyValue(mockResponse.getRequestBody())
                    .headers(c -> c.putAll(mockResponse.getRequestHeaders()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(REQUEST_TIMEOUT);
            log.info("Callback request response: {}", response);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public Optional<String> executeRequest(String requestId) {
        return configRepository.findRequest(requestId)
                .filter(r -> !r.isDisabled())
                .map(this::executeRequest);
    }

    private String executeRequest(OutboundRequest request) {
        log.info("Executing request: {}", request.getId());
        try {
            String response = WebClient.create()
                    .method(request.getMethod().asHttpMethod())
                    .uri(request.getPath())
                    .bodyValue(request.getBody())
                    //.headers(c -> c.putAll(mockResponse.getRequestHeaders()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(REQUEST_TIMEOUT);
            log.info("Request ({}) response: {}", request.getId(), response);
            return response;
        } catch (Exception e) {
            log.error("Request (" + request.getId() + ") error.", e);
        }
        return "";
    }

    @Override
    public Optional<OutboundRequest> getEnabledRequest(String requestId) {
        return configRepository.findRequest(requestId)
                .filter(r -> !r.isDisabled());
    }

    @Override
    public List<OutboundRequestDto> getRequests() {
        return requestMapper.toDto(configRepository.findAllRequests());
    }

    @Override
    public void putRequest(OutboundRequestDto existing, OutboundRequestDto request) throws IOException {
        configRepository.putRequest(requestMapper.fromDto(existing), requestMapper.fromDto(request));
    }

    @Override
    public void putRequests(List<OutboundRequestDto> dto, boolean overwrite) throws IOException {
        configRepository.putRequests(requestMapper.fromDto(dto), overwrite);
    }

    @Override
    public void deleteRequests(List<OutboundRequestDto> dto) throws IOException {
        configRepository.deleteRequests(requestMapper.fromDto(dto));
    }
}
