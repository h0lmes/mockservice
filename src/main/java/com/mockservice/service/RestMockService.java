package com.mockservice.service;

import com.mockservice.request.HttpRequestFacade;
import com.mockservice.request.JsonHttpRequestFacade;
import com.mockservice.resource.JsonMockResource;
import com.mockservice.resource.MockResource;
import com.mockservice.util.ResourceReader;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Service
@Primary
public class RestMockService implements MockService {

    private final ResourceLoader resourceLoader;
    private final HttpServletRequest request;
    private final ConcurrentLruCache<String, MockResource> resourceCache;

    public RestMockService(ResourceLoader resourceLoader, HttpServletRequest request) {
        this.resourceLoader = resourceLoader;
        this.request = request;
        resourceCache = new ConcurrentLruCache<>(256, this::loadResource);
    }

    private MockResource loadResource(String path) {
        return new JsonMockResource(ResourceReader.asString(resourceLoader.getResource(path)));
    }

    @Override
    public ResponseEntity<String> mock(String folder, Map<String, String> variables) {
        HttpRequestFacade requestFacade = new JsonHttpRequestFacade(request, folder);
        MockResource resource = resourceCache.get(requestFacade.getPath());
        requestFacade.mockTimeout();
        Map<String, String> requestVariables = requestFacade.getVariables(variables);
        return ResponseEntity
                .status(resource.getCode())
                .headers(resource.getHeaders())
                .body(resource.getBody(requestVariables));
    }
}
