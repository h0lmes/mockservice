package com.mockservice.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

@Component
public class WebClientFactory {
    private static final Logger log = LoggerFactory.getLogger(WebClientFactory.class);

    private final String certFile;
    private final String certPassword;

    public WebClientFactory(@Value("${application.ssl.cert-file}") String certFile,
                            @Value("${application.ssl.password}") String certPassword) {
        this.certFile = certFile;
        this.certPassword = certPassword;
    }

    public WebClient create(boolean secure) {
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
}
