package com.mockservice.service;

import com.mockservice.request.HttpRequestFacade;
import com.mockservice.request.JsonHttpRequestFacade;
import com.mockservice.resource.JsonMockResource;
import com.mockservice.resource.MockResource;
import com.mockservice.template.TemplateEngine;
import com.mockservice.util.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

@Service
@Primary
public class RestMockService implements MockService {

    private static final Logger log = LoggerFactory.getLogger(RestMockService.class);

    private final ResourceLoader resourceLoader;
    private final HttpServletRequest request;
    private final ConcurrentLruCache<String, MockResource> resourceCache;
    private final TemplateEngine engine;

    public RestMockService(ResourceLoader resourceLoader, HttpServletRequest request, TemplateEngine engine) {
        this.resourceLoader = resourceLoader;
        this.request = request;
        this.engine = engine;
        resourceCache = new ConcurrentLruCache<>(256, this::loadResource);
    }

    private MockResource loadResource(String path) {
        try {
            return new JsonMockResource(engine, ResourceReader.asStringOrFind(resourceLoader, path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ResponseEntity<String> mock(String folder, Map<String, String> variables) {
        HttpRequestFacade requestFacade = new JsonHttpRequestFacade(request, folder);
        String path = requestFacade.getPath();
        log.info("File requested: {}", path);
        MockResource resource = resourceCache.get(path);
        requestFacade.mockTimeout();
        Map<String, String> requestVariables = requestFacade.getVariables(variables);
        return ResponseEntity
                .status(resource.getCode())
                .headers(resource.getHeaders())
                .body(resource.getBody(requestVariables));
    }
}
