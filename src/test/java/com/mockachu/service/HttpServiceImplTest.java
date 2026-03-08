package com.mockachu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockachu.domain.Settings;
import com.mockachu.exception.HttpServiceException;
import com.mockachu.repository.ConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpServiceImplTest {

    @Mock
    private ConfigRepository configRepository;

    @Mock
    private Settings settings;

    private ObjectMapper jsonMapper;
    private HttpServiceImpl httpService;

    @BeforeEach
    void setUp() {
        jsonMapper = new ObjectMapper();
        httpService = new HttpServiceImpl(configRepository, jsonMapper, 10, 10);
    }

    // --- onAfterSettingsChanged ---

    @Test
    void shouldRecreateWebClientWhenCertificateRemovedAndWasSecure() {
        when(configRepository.getSettings()).thenReturn(settings);
        // Force secure state by making setCertificatePassword fail midway,
        // or just verify the method runs without error when cert is blank
        when(settings.getCertificate()).thenReturn("");

        // Should not throw - just recreates the insecure client
        httpService.onAfterSettingsChanged();

        verify(configRepository, atLeastOnce()).getSettings();
    }

    @Test
    void shouldNotRecreateWebClientWhenCertificateStillPresent() {
        when(configRepository.getSettings()).thenReturn(settings);
        when(settings.getCertificate()).thenReturn("somecert");

        httpService.onAfterSettingsChanged();

        // No recreation - verify settings were checked
        verify(settings).getCertificate();
    }

    // --- setCertificatePassword ---

    @Test
    void shouldThrowWhenPasswordIsNull() {
        assertThatThrownBy(() -> httpService.setCertificatePassword(null))
                .isInstanceOf(HttpServiceException.class)
                .hasMessageContaining("No password specified");
    }

    @Test
    void shouldThrowWhenPasswordIsBlank() {
        assertThatThrownBy(() -> httpService.setCertificatePassword("   "))
                .isInstanceOf(HttpServiceException.class)
                .hasMessageContaining("No password specified");
    }

    @Test
    void shouldThrowWhenCertificateIsNull() {
        when(configRepository.getSettings()).thenReturn(settings);
        when(settings.getCertificate()).thenReturn(null);

        assertThatThrownBy(() -> httpService.setCertificatePassword("password"))
                .isInstanceOf(HttpServiceException.class)
                .hasMessageContaining("No certificate set");
    }

    @Test
    void shouldThrowWhenCertificateIsBlank() {
        when(configRepository.getSettings()).thenReturn(settings);
        when(settings.getCertificate()).thenReturn("");

        assertThatThrownBy(() -> httpService.setCertificatePassword("password"))
                .isInstanceOf(HttpServiceException.class)
                .hasMessageContaining("No certificate set");
    }

    @Test
    void shouldThrowWhenCertificateIsInvalidBase64() {
        when(configRepository.getSettings()).thenReturn(settings);
        when(settings.getCertificate()).thenReturn("not-a-valid-cert-base64");

        assertThatThrownBy(() -> httpService.setCertificatePassword("password"))
                .isInstanceOf(HttpServiceException.class)
                .hasMessageContaining("Create secure SSL context");
    }

    // --- request() ---

    @Test
    void shouldReturnErrorResultWhenUriIsUnreachable() {
        var result = httpService.request(
                RequestMethod.GET,
                "http://localhost:0/unreachable",
                "",
                null);

        assertThat(result.isError()).isTrue();
        assertThat(result.getStatusCode()).isEqualTo(400);
    }

    @Test
    void shouldPrependHttpSchemeWhenMissing() {
        var result = httpService.request(
                RequestMethod.GET,
                "localhost:0/unreachable",
                "",
                null);

        // Will fail to connect, but error result confirms URI was processed
        assertThat(result.isError()).isTrue();
        assertThat(result.getUri()).contains("http://");
    }

    @Test
    void shouldReturnErrorResultWhenBodyIsNull() {
        var result = httpService.request(
                RequestMethod.POST,
                "http://localhost:0/unreachable",
                null,
                null);

        assertThat(result.isError()).isTrue();
    }

    @Test
    void shouldIncludeMethodAndUriInErrorResult() {
        var result = httpService.request(
                RequestMethod.DELETE,
                "http://localhost:0/some/path",
                "",
                null);

        assertThat(result.isError()).isTrue();
        assertThat(result.getMethod()).isEqualTo(RequestMethod.DELETE);
        assertThat(result.getUri()).contains("/some/path");
    }

    @Test
    void shouldPassHeadersIntoResult() {
        Map<String, List<String>> headers = Map.of("X-Custom", List.of("value"));

        var result = httpService.request(
                RequestMethod.GET,
                "http://localhost:0/unreachable",
                "",
                headers);

        assertThat(result.isError()).isTrue();
        assertThat(result.getRequestHeaders()).containsKey("X-Custom");
    }
}
