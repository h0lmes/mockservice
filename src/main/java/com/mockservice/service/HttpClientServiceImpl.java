package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.exception.RequestServiceRequestException;
import com.mockservice.model.HttpRequestResult;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.template.MockVariables;
import com.mockservice.util.MapUtils;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Setting up SSL certificates.
 *
 * 1. Generate cert.pem and key.pem files:
 *
 *      openssl genrsa -out key.pem 2048
 *      openssl req -new -key key.pem -out csr.pem
 *      openssl x509 -req -in csr.pem -signkey key.pem -out cert.pem
 *
 * 2. Verify that cert.pem and key.pem files are valid:
 *
 *      openssl x509 -noout -modulus -in cert.pem | openssl md5
 *      openssl rsa -noout -modulus -in key.pem | openssl md5
 *
 * 3. Convert cert and key files from PEM to a PKCS12 for Java:
 *
 *      openssl pkcs12 -export -in cert.pem -inkey key.pem -out cert.p12
 *
 * (or on Windows)
 *
 *      openssl pkcs12 -export -in cert.pem -inkey key.pem -out cert.p12 -password pass:YOUR_PASSWORD
 */
@Service
public class HttpClientServiceImpl implements HttpClientService {
    private static final Logger log = LoggerFactory.getLogger(HttpClientServiceImpl.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final ConfigRepository configRepository;
    private final ObjectMapper jsonMapper;
    private final ConnectionProvider connectionProvider;
    private WebClient webClient;

    public HttpClientServiceImpl(
            ConfigRepository configRepository,
            @Qualifier("jsonMapper") ObjectMapper jsonMapper,
            @Value("${application.client.max-connections:2000}") Integer maxConnections,
            @Value("${application.client.max-pending:2000}") Integer maxPending) {
        this.configRepository = configRepository;
        this.jsonMapper = jsonMapper;
        connectionProvider = ConnectionProvider.builder("fixed")
                .maxConnections(maxConnections)
                .pendingAcquireMaxCount(maxPending).build();
        webClient = create(false, "");
    }

    @Override
    public void setCertificatePassword(String password) throws Exception {
        webClient = create(true, password);
    }

    @Override
    public Optional<HttpRequestResult> request(RequestMethod method,
                        String uri,
                        String requestBody,
                        Map<String, List<String>> headers) {
        MockVariables responseVars;
        var startMillis = System.currentTimeMillis();
        try {
            log.info("Executing request: {} {}", method, uri);
            String body = webClient
                    .method(method.asHttpMethod())
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
            log.info("Request ({} {}) response:\n{}", method, uri, e.toString());
            responseVars = jsonToMap(e.getBody());
            return Optional.of(
                    new HttpRequestResult(false, method, uri,
                            headers, requestBody, e.getBody(),
                            responseVars, e.getCode(), startMillis));
        } catch (Exception e) {
            log.error("Request (" + method + " " + uri + ") error.", e);
            return Optional.of(
                    new HttpRequestResult(true, method, uri,
                            headers, requestBody, e.getMessage(),
                            MockVariables.empty(), 0, startMillis));
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

    public WebClient create(boolean secure, String password) {
        HttpClient httpClient = HttpClient.create(connectionProvider);
        if (secure) {
            byte[] certBytes = Base64.decodeBase64(configRepository.getSettings().getCertificate());
            SslContext sslContext = getSSLContext(certBytes, password);
            log.info("New SSL context created");
            httpClient = httpClient.secure(sslSpec -> sslSpec.sslContext(sslContext));
        }
        ClientHttpConnector clientHttpConnector = new ReactorClientHttpConnector(httpClient);
        return WebClient.builder().clientConnector(clientHttpConnector).build();
    }

    private SslContext getSSLContext(byte[] certBytes, String password) {
        try (var inputStream = new ByteArrayInputStream(certBytes)) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(inputStream, password.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, password.toCharArray());
            return SslContextBuilder.forClient()
                    .keyManager(keyManagerFactory)
                    .trustManager(trustManagerFactory)
                    .build();
        } catch (Exception e) {
            log.error("Error creating SSL context: ", e);
            throw new RuntimeException("Error creating SSL context: " + e.getMessage());
        }
    }
}
