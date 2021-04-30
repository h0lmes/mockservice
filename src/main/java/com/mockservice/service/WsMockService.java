package com.mockservice.service;

import com.mockservice.request.HttpRequestFacade;
import com.mockservice.request.XmlHttpRequestFacade;
import com.mockservice.resource.MockResource;
import com.mockservice.resource.XmlMockResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;

import java.util.Map;

@Service("wsMockService")
public class WsMockService extends AbstractMockService implements MockService {

    private final ConcurrentLruCache<String, MockResource> resourceCache;

    public WsMockService(ResourceLoader resourceLoader) {
        super(resourceLoader);
        resourceCache = new ConcurrentLruCache<>(256, this::loadResource);
    }

    private MockResource loadResource(String path) {
        return new XmlMockResource(loadResourceString(path));
    }

    public ResponseEntity<String> mock(String folder, Map<String, String> variables) {
        HttpRequestFacade requestFacade = new XmlHttpRequestFacade(request, folder);
        MockResource resource = resourceCache.get(requestFacade.getPath());
        Map<String, String> requestVariables = requestFacade.getVariables(variables);
        String body = resource.getBody(requestVariables);
        return ResponseEntity.status(resource.getCode()).headers(resource.getHeaders()).body(body);
    }
}
