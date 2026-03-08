package com.mockachu.web.webapp;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WebAppForwardFilterTest {

    private WebAppForwardFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;
    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setUp() throws Exception {
        filter = new WebAppForwardFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        responseBody = new ByteArrayOutputStream();
        ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public void write(int b) { responseBody.write(b); }
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setWriteListener(jakarta.servlet.WriteListener l) {}
        };
        when(response.getOutputStream()).thenReturn(servletOutputStream);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/", "/about", "/config", "/context", "/generate",
            "/import", "/kafka", "/log", "/request-graph", "/settings"
    })
    void shouldServeIndexHtmlForSpaRoutes(String uri) throws Exception {
        when(request.getRequestURI()).thenReturn(uri);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setContentType("text/html;charset=UTF-8");
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(responseBody.size()).isGreaterThan(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/_nuxt/app.js",
            "/_nuxt/r3huVJNv.js",
            "/_nuxt/style.css",
            "/_nuxt/logo.svg",
            "/_nuxt/icon.png",
            "/_nuxt/favicon.ico"
    })
    void shouldServeStaticAssetsFromNuxtPath(String uri) throws Exception {
        when(request.getRequestURI()).thenReturn(uri);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldReturn404WhenNuxtAssetNotFound() throws Exception {
        when(request.getRequestURI()).thenReturn("/_nuxt/nonexistent-file-xyz.js");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND,
                "/webapp/_nuxt/nonexistent-file-xyz.js not found");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/v1/users",
            "/api/robot",
            "/products",
            "/v2/store/order",
            "/actuator/health"
    })
    void shouldPassThroughNonSpaRoutes(String uri) throws Exception {
        when(request.getRequestURI()).thenReturn(uri);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setContentType(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/_nuxt/app.js", "/_nuxt/chunk.js", "/main.js"})
    void shouldResolveJavaScriptContentType(String uri) {
        assertThat(filter.resolveContentType(uri)).isEqualTo("application/javascript;charset=UTF-8");
    }

    @Test
    void shouldResolveCssContentType() {
        assertThat(filter.resolveContentType("/style.css")).isEqualTo("text/css;charset=UTF-8");
    }

    @Test
    void shouldResolveSvgContentType() {
        assertThat(filter.resolveContentType("/logo.svg")).isEqualTo("image/svg+xml");
    }

    @Test
    void shouldResolvePngContentType() {
        assertThat(filter.resolveContentType("/icon.png")).isEqualTo("image/png");
    }

    @Test
    void shouldResolveIcoContentType() {
        assertThat(filter.resolveContentType("/favicon.ico")).isEqualTo("image/x-icon");
    }

    @Test
    void shouldResolveOctetStreamForUnknownExtension() {
        assertThat(filter.resolveContentType("/data.bin")).isEqualTo("application/octet-stream");
    }
}
