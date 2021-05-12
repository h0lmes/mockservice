package com.mockservice.service;

import com.mockservice.request.RequestFacade;
import com.mockservice.request.RestRequestFacade;
import com.mockservice.resource.MockResource;
import com.mockservice.resource.RestMockResource;
import com.mockservice.template.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

@Service("rest")
public class RestMockService implements MockService {

    private static final Logger log = LoggerFactory.getLogger(RestMockService.class);

    private final HttpServletRequest request;
    private final ResourceService resourceService;
    private final TemplateEngine templateEngine;
    private final ConcurrentLruCache<String, MockResource> resourceCache;

    public RestMockService(@Autowired HttpServletRequest request,
                           @Autowired ResourceService resourceService,
                           @Autowired TemplateEngine templateEngine) {
        this.request = request;
        this.resourceService = resourceService;
        this.templateEngine = templateEngine;
        resourceCache = new ConcurrentLruCache<>(256, this::loadResource);
    }

    private MockResource loadResource(String path) {
        try {
            return new RestMockResource(templateEngine, resourceService.load(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ResponseEntity<String> mock(String folder, Map<String, String> variables) {
        RequestFacade requestFacade = new RestRequestFacade(request, folder);
        String path = requestFacade.getPath();
        log.info("File requested: {}", path);
        MockResource resource = resourceCache.get(path);
        Map<String, String> requestVariables = requestFacade.getVariables(variables);
        return ResponseEntity
                .status(resource.getCode())
                .headers(resource.getHeaders())
                .body(resource.getBody(requestVariables));
    }
}
