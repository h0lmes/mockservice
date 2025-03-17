package com.mockservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.OutboundRequest;
import com.mockservice.mapper.OutboundRequestMapper;
import com.mockservice.model.OutboundRequestDto;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.template.MockVariables;
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

    public RequestServiceImpl(ConfigRepository configRepository,
                              OutboundRequestMapper requestMapper,
                              TemplateEngine templateEngine,
                              @Qualifier("jsonMapper") ObjectMapper jsonMapper,
                              @Value("${application.ssl.cert-file}") String certFile,
                              @Value("${application.ssl.password}") String certPassword) {
        this.configRepository = configRepository;
        this.requestMapper = requestMapper;
        this.templateEngine = templateEngine;
        this.jsonMapper = jsonMapper;
        this.certFile = certFile;
        this.certPassword = certPassword;
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
            findAndExecuteRequest(requestId, variables, level);
        }
    }

    @Override
    public Optional<String> executeRequest(String requestId, MockVariables variables) {
        return findAndExecuteRequest(requestId, variables, 0);
    }

    private Optional<String> findAndExecuteRequest(String requestId, MockVariables variables, int level) {
        return configRepository.findRequest(requestId)
                .filter(r -> !r.isDisabled())
                .map(r -> executeRequest(r, variables, level));
    }

    private String executeRequest(OutboundRequest request, MockVariables variables, int level) {
        if (level > MAX_LEVEL_FOR_TRIGGERED_REQUEST) {
            // prevent infinite loops of requests
            log.info("Max level for triggered request reached for: {}", request);
            return "";
        }

        log.info("Executing request: {}", request.getId());
        MockVariables responseVars = new MockVariables();
        try {
            StringTemplate requestBody = new StringTemplate();
            requestBody.add(request.getBody());
            String uri = request.getPath().contains("://") ? request.getPath() : "http://" + request.getPath();
            String response = getWebClient(uri.startsWith("https://"))
                    .method(request.getMethod().asHttpMethod())
                    .uri(uri)
                    .bodyValue(requestBody.toString(variables, templateEngine.getFunctions()))
                    //.headers(c -> c.putAll(mockResponse.getRequestHeaders()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(REQUEST_TIMEOUT);
            log.info("Request ({}) response:\n{}", request.getId(), response);
            responseVars.putAll(MapUtils.flattenMap(MapUtils.jsonToMap(response, jsonMapper)));
            return response;
        } catch (Exception e) {
            log.error("Request (" + request.getId() + ") error.", e);
        } finally {
            if (request.isTriggerRequest()) {
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
