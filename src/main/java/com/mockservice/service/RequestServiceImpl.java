package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.OutboundRequest;
import com.mockservice.exception.RequestServiceRequestException;
import com.mockservice.mapper.OutboundRequestMapper;
import com.mockservice.model.OutboundRequestDto;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.template.MockVariables;
import com.mockservice.template.RequestHeadersTemplate;
import com.mockservice.template.StringTemplate;
import com.mockservice.template.TemplateEngine;
import com.mockservice.util.MapUtils;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Setting up SSL certificates.
 *
 * 1.
 * Verify that .cert and .key files are valid:
 *
 *      openssl x509 -noout -modulus -in cert.crt | openssl md5
 *      #> (stdin)= 7f1a9c4d13aead7fd4a0f241a6ce8
 * and
 *      openssl rsa -noout -modulus -in cert.key | openssl md5
 *      #> (stdin)= 7f1a9c4d13aead7fd4a0f241a6ce8
 *
 * 2.
 * Convert .cert and .key files from PEM to a PKCS12 that Java can understand
 * (this step will prompt for a password):
 *
 *      openssl pkcs12 -export -in cert.crt -in cert.key -out cert.p12
 */
@Service
public class RequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceImpl.class);

    private static final long EXECUTION_DELAY_MILLISECONDS = 100;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final int MAX_LEVEL_FOR_TRIGGERED_REQUEST = 3;

    private final ConfigRepository configRepository;
    private final OutboundRequestMapper requestMapper;
    private final TemplateEngine templateEngine;
    private final ObjectMapper jsonMapper;
    private final String certFile;
    private final String certPassword;
    private final ConcurrentLruCache<OutboundRequest, RequestHeadersTemplate> headersCache;

    public RequestServiceImpl(ConfigRepository configRepository,
                              OutboundRequestMapper requestMapper,
                              TemplateEngine templateEngine,
                              @Value("${application.request-service.cache-size}") int cacheSize,
                              @Qualifier("jsonMapper") ObjectMapper jsonMapper,
                              @Value("${application.ssl.cert-file}") String certFile,
                              @Value("${application.ssl.password}") String certPassword) {
        this.configRepository = configRepository;
        this.requestMapper = requestMapper;
        this.templateEngine = templateEngine;
        this.jsonMapper = jsonMapper;
        this.certFile = certFile;
        this.certPassword = certPassword;
        headersCache = new ConcurrentLruCache<>(cacheSize, this::getRequestHeadersTemplate);
    }

    private RequestHeadersTemplate getRequestHeadersTemplate(OutboundRequest request) {
        return new RequestHeadersTemplate(request.getHeaders());
    }

    @Override
    public void schedule(String requestIds, MockVariables variables) {
        scheduleRequests(requestIds, variables, 0);
    }

    private void scheduleRequests(String requestIds, MockVariables variables, int level) {
        if (Strings.isBlank(requestIds)) return;
        CompletableFuture.runAsync(
                () -> executeRequests(requestIds, variables, level),
                CompletableFuture.delayedExecutor(EXECUTION_DELAY_MILLISECONDS, TimeUnit.MILLISECONDS)
        );
    }

    private void executeRequests(String requestIds, MockVariables variables, int level) {
        for (String requestId : requestIds.split(",")) {
            findAndExecuteRequest(requestId, variables, level, true);
        }
    }

    @Override
    public Optional<String> executeRequest(String requestId, MockVariables variables, boolean allowTrigger) {
        return findAndExecuteRequest(requestId, variables, 0, allowTrigger);
    }

    private Optional<String> findAndExecuteRequest(String requestId,
                                                   MockVariables variables,
                                                   int level,
                                                   boolean allowTrigger) {
        return getEnabledRequest(requestId)
                .map(r -> executeRequest(r, variables, level, allowTrigger));
    }

    private String executeRequest(OutboundRequest request,
                                  MockVariables variables,
                                  int level,
                                  boolean allowTrigger) {
        if (level > MAX_LEVEL_FOR_TRIGGERED_REQUEST) {
            // try preventing infinite loop of requests
            log.info("Max level for triggered request reached for: {}", request);
            return "";
        }

        log.info("Executing request: {}", request.getId());
        MockVariables responseVars = new MockVariables();

        try {
            StringTemplate requestBody = new StringTemplate();
            requestBody.add(request.getBody());
            String uri = request.getPath().contains("://") ? request.getPath() : "http://" + request.getPath();
            boolean secure = uri.startsWith("https://");
            var headers = headersCache.get(request)
                    .toMap(variables, templateEngine.getFunctions());

            String body = getWebClient(secure)
                    .method(request.getMethod().asHttpMethod())
                    .uri(uri)
                    .bodyValue(requestBody.toString(variables, templateEngine.getFunctions()))
                    .headers(c -> c.putAll(headers))
                    .retrieve()
                    // wrap any response into exception to extract both StatusCode and Body
                    .onStatus(code -> true,
                            res -> res.bodyToMono(String.class)
                                    .handle((error, sink) -> sink.error(
                                            new RequestServiceRequestException(res.statusCode().value(), error)
                                    )
                    ))
                    .bodyToMono(String.class)
                    .block(REQUEST_TIMEOUT);

            throw new RequestServiceRequestException(200, body); // just in case
        } catch (RequestServiceRequestException e) {
            try {
                responseVars.putAll(MapUtils.flattenMap(MapUtils.jsonToMap(e.getBody(), jsonMapper)));
            } catch (JsonProcessingException jpe) {
                log.warn("Request; invalid JSON:\n{}", jpe.getMessage());
            }
            log.info("Request ({}) response:\n{}", request.getId(), e.toString());
            return e.toString();
        } catch (Exception e) {
            log.error("Request (" + request.getId() + ") error.", e);
        } finally {
            if (allowTrigger && request.isTriggerRequest()) {
                scheduleRequests(request.getTriggerRequestIds(), responseVars, level + 1);
            }
        }
        return "";
    }

    private WebClient getWebClient(boolean secure) {
        if (!secure) return WebClient.create();

        SslContext sslContext = getSSLContext();
        if (sslContext == null) return WebClient.create();

        HttpClient httpClient = HttpClient.create().secure(sslSpec -> sslSpec.sslContext(sslContext));
        ClientHttpConnector clientHttpConnector = new ReactorClientHttpConnector(httpClient);
        return WebClient.builder()
                .clientConnector(clientHttpConnector)
                .build();
    }

    private SslContext getSSLContext() {
        try (FileInputStream keyStoreFileInputStream = new FileInputStream(certFile)) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreFileInputStream, certPassword.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, certPassword.toCharArray());
            return SslContextBuilder.forClient()
                    .keyManager(keyManagerFactory)
                    .build();
        } catch (Exception e) {
            log.error("An error has occurred while creating SSL context: ", e);
        }
        return null;
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
        var ex = requestMapper.fromDto(existing);
        headersCache.remove(ex);
        configRepository.putRequest(ex, requestMapper.fromDto(request));
    }

    @Override
    public void putRequests(List<OutboundRequestDto> dto, boolean overwrite) throws IOException {
        var requests = requestMapper.fromDto(dto);
        requests.forEach(headersCache::remove);
        configRepository.putRequests(requests, overwrite);
    }

    @Override
    public void deleteRequests(List<OutboundRequestDto> dto) throws IOException {
        var requests = requestMapper.fromDto(dto);
        requests.forEach(headersCache::remove);
        configRepository.deleteRequests(requests);
    }
}
