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
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

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

        HttpClient httpClient = HttpClient.create()
                .secure(sslSpec -> sslSpec.sslContext(sslContext));
        ClientHttpConnector clientHttpConnector = new ReactorClientHttpConnector(httpClient);
        return WebClient.builder().clientConnector(clientHttpConnector).build();
    }

    private SslContext getSSLContext() {
        try (FileInputStream fis = new FileInputStream(certFile)) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(fis, certPassword.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, certPassword.toCharArray());
            return SslContextBuilder.forClient()
                    .keyManager(keyManagerFactory)
                    .trustManager(trustManagerFactory)
                    .build();
        } catch (Exception e) {
            log.error("Error creating SSL context: ", e);
            return null;
        }
    }
}
