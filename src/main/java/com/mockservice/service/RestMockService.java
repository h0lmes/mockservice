package com.mockservice.service;

import com.mockservice.request.HttpRequestFacade;
import com.mockservice.request.JsonHttpRequestFacade;
import com.mockservice.resource.JsonMockResource;
import com.mockservice.resource.MockResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;

import java.util.Map;

@Service("restMockService")
public class RestMockService extends AbstractMockService implements MockService {

    private final ConcurrentLruCache<String, MockResource> resourceCache;

    public RestMockService(ResourceLoader resourceLoader) {
        super(resourceLoader);
        resourceCache = new ConcurrentLruCache<>(256, this::loadResource);
    }

    private MockResource loadResource(String path) {
        return new JsonMockResource(loadResourceString(path));
    }

    @Override
    public ResponseEntity<String> mock(String folder, Map<String, String> variables) {
        HttpRequestFacade requestFacade = new JsonHttpRequestFacade(request, folder);
        MockResource resource = resourceCache.get(requestFacade.getPath());
        requestFacade.mockTimeout();
        Map<String, String> requestVariables = requestFacade.getVariables(variables);
        String body = resource.getBody(requestVariables);
        return ResponseEntity.status(resource.getCode()).headers(resource.getHeaders()).body(body);
    }
}
