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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.annotation.Nullable;
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
    private final VariablesService variablesService;
    private final ObjectMapper jsonMapper;
    private final String certFile;
    private final String certPassword;
    private final ConcurrentLruCache<OutboundRequest, RequestHeadersTemplate> headersCache;
    private final ConcurrentLruCache<OutboundRequest, StringTemplate> uriCache;
    private final ConcurrentLruCache<OutboundRequest, StringTemplate> requestBodyCache;

    public RequestServiceImpl(ConfigRepository configRepository,
                              OutboundRequestMapper requestMapper,
                              TemplateEngine templateEngine,
                              VariablesService variablesService,
                              @Value("${application.request-service.cache-size}") int cacheSize,
                              @Qualifier("jsonMapper") ObjectMapper jsonMapper,
                              @Value("${application.ssl.cert-file}") String certFile,
                              @Value("${application.ssl.password}") String certPassword) {
        this.configRepository = configRepository;
        this.requestMapper = requestMapper;
        this.templateEngine = templateEngine;
        this.variablesService = variablesService;
        this.jsonMapper = jsonMapper;
        this.certFile = certFile;
        this.certPassword = certPassword;
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
    public void schedule(String requestIds,
                         @Nullable MockVariables variables) {
        scheduleRequests(requestIds, variables, 0);
    }

    @Override
    public Optional<String> executeRequest(String requestId,
                                           @Nullable MockVariables variables,
                                           boolean allowTrigger) {
        return executeRequestById(requestId, variables, 0, allowTrigger);
    }

    private void scheduleRequests(String requestIds,
                                  @Nullable MockVariables variables,
                                  int level) {
        if (Strings.isBlank(requestIds)) return;
        CompletableFuture.runAsync(
                () -> executeRequests(requestIds, variables, level),
                CompletableFuture.delayedExecutor(EXECUTION_DELAY_MILLISECONDS, TimeUnit.MILLISECONDS)
        );
    }

    private void executeRequests(String requestIds,
                                 @Nullable MockVariables variables,
                                 int level) {
        for (String requestId : requestIds.split(",")) {
            executeRequestById(requestId, variables, level, true);
        }
    }

    private Optional<String> executeRequestById(String requestId,
                                                @Nullable MockVariables variables,
                                                int level,
                                                boolean allowTrigger) {
        return getEnabledRequest(requestId)
                .map(r -> executeRequest(r, variables, level, allowTrigger));
    }

    private String executeRequest(OutboundRequest request,
                                  @Nullable MockVariables inVariables,
                                  int level,
                                  boolean allowTrigger) {
        if (level > MAX_LEVEL_FOR_TRIGGERED_REQUEST) {
            // try preventing infinite loop of requests
            log.info("Max level for triggered request reached for: {}", request);
            return "";
        }

        MockVariables requestVars = composeVariables(variablesService.getAll(), inVariables);
        MockVariables responseVars = new MockVariables();

        var requestBody = requestBodyCache.get(request)
                .toString(requestVars, templateEngine.getFunctions());
        String uri = uriCache.get(request)
                .toString(requestVars, templateEngine.getFunctions());
        var headersTemplate = headersCache.get(request);
        var headers = headersTemplate
                .toMap(requestVars, templateEngine.getFunctions());

        boolean secure = uri.startsWith("https://");
        var startMillis = System.currentTimeMillis();

        try {
            log.info("Executing request: {}", request.getId());
            String body = getWebClient(secure)
                    .method(request.getMethod().asHttpMethod())
                    .uri(uri)
                    .bodyValue(requestBody)
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
            log.info("Request ({}) response:\n{}", request.getId(), e.toString());
            try {
                responseVars.putAll(MapUtils.flattenMap(MapUtils.jsonToMap(e.getBody(), jsonMapper)));
            } catch (JsonProcessingException jpe) {
                log.warn("Request; invalid JSON:\n{}", jpe.getMessage());
            }
            return formatResponse(
                    request.getMethod(),
                    uri,
                    headersTemplate,
                    requestBody,
                    e.toString(),
                    startMillis);
        } catch (Exception e) {
            log.error("Request (" + request.getId() + ") error.", e);
            return "Request [" + request.getId() + "] error.\n" + e;
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

    private MockVariables composeVariables(@Nullable MockVariables global,
                                           @Nullable MockVariables inVariables) {
        if (inVariables!= null && global!= null &&
                !inVariables.isEmpty() && !global.isEmpty()) {
            return new MockVariables().putAll(global).putAll(inVariables);
        }
        if (inVariables!= null && !inVariables.isEmpty()) {
            return inVariables;
        }
        if (global!= null && !global.isEmpty()) {
            return global;
        }
        return null;
    }

    private String formatResponse(RequestMethod method,
                                  String uri,
                                  RequestHeadersTemplate headersTemplate,
                                  String requestBody,
                                  String response,
                                  long startMillis) {
        var elapsedMillis = System.currentTimeMillis() - startMillis;
        StringBuilder builder = new StringBuilder();
        builder.append(method.toString()).append(" ").append(uri).append('\n');
        if (!headersTemplate.isEmpty()) {
            builder.append(headersTemplate).append("\n\n");
        }
        if (!requestBody.isEmpty()) {
            builder.append(requestBody).append("\n\n");
        }
        builder.append("--- response in ").append(elapsedMillis).append(" ms ---\n")
                .append(response);
        return builder.toString();
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
        uriCache.remove(ex);
        requestBodyCache.remove(ex);
        configRepository.putRequest(ex, requestMapper.fromDto(request));
    }

    @Override
    public void putRequests(List<OutboundRequestDto> dto, boolean overwrite) throws IOException {
        var requests = requestMapper.fromDto(dto);
        requests.forEach(r -> {
            headersCache.remove(r);
            uriCache.remove(r);
            requestBodyCache.remove(r);
        });
        configRepository.putRequests(requests, overwrite);
    }

    @Override
    public void deleteRequests(List<OutboundRequestDto> dto) throws IOException {
        var requests = requestMapper.fromDto(dto);
        requests.forEach(r -> {
            headersCache.remove(r);
            uriCache.remove(r);
            requestBodyCache.remove(r);
        });
        configRepository.deleteRequests(requests);
    }
}
