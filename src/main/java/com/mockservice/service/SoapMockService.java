package com.mockservice.service;

import com.mockservice.request.HttpRequestFacade;
import com.mockservice.request.XmlHttpRequestFacade;
import com.mockservice.resource.MockResource;
import com.mockservice.resource.XmlMockResource;
import com.mockservice.util.ResourceReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Service("soapMockService")
public class SoapMockService implements MockService {

    @Autowired
    HttpServletRequest request;
    private final ResourceLoader resourceLoader;
    private final ConcurrentLruCache<String, MockResource> resourceCache;

    public SoapMockService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        resourceCache = new ConcurrentLruCache<>(256, this::loadResource);
    }

    private MockResource loadResource(String path) {
        return new XmlMockResource(ResourceReader.asString(resourceLoader.getResource(path)));
    }

    public ResponseEntity<String> mock(String folder, Map<String, String> variables) {
        HttpRequestFacade requestFacade = new XmlHttpRequestFacade(request, folder);
        MockResource resource = resourceCache.get(requestFacade.getPath());
        Map<String, String> requestVariables = requestFacade.getVariables(variables);
        return ResponseEntity
                .status(resource.getCode())
                .headers(resource.getHeaders())
                .body(resource.getBody(requestVariables));
    }
}
