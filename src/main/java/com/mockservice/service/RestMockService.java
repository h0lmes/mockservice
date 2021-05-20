package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.config.RegisteredRoutesHolder;
import com.mockservice.mockconfig.Route;
import com.mockservice.mockconfig.RouteType;
import com.mockservice.request.RequestFacade;
import com.mockservice.request.RestRequestFacade;
import com.mockservice.resource.MockResource;
import com.mockservice.resource.RestMockResource;
import com.mockservice.service.exception.RouteNotFoundException;
import com.mockservice.service.model.RestErrorResponse;
import com.mockservice.template.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

@Service("rest")
public class RestMockService implements MockService {

    private static final Logger log = LoggerFactory.getLogger(RestMockService.class);

    private final HttpServletRequest request;
    private final ResourceService resourceService;
    private final TemplateEngine templateEngine;
    private final ConfigService configService;
    private final RegisteredRoutesHolder registeredRoutesHolder;
    private final ConcurrentLruCache<String, MockResource> resourceCache;

    public RestMockService(HttpServletRequest request,
                           ResourceService resourceService,
                           TemplateEngine templateEngine,
                           ConfigService configService,
                           RegisteredRoutesHolder registeredRoutesHolder,
                           @Value("${application.cache.rest-resource}") int cacheSizeLimit) {
        this.request = request;
        this.resourceService = resourceService;
        this.templateEngine = templateEngine;
        this.configService = configService;
        this.registeredRoutesHolder = registeredRoutesHolder;
        resourceCache = new ConcurrentLruCache<>(cacheSizeLimit, this::loadResource);
    }

    private MockResource loadResource(String path) {
        try {
            return new RestMockResource(templateEngine, resourceService.load(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ResponseEntity<String> mock(Map<String, String> variables) {
        RequestFacade requestFacade = new RestRequestFacade(request);

//        String group = registeredRoutesHolder
//                .getRegisteredRoute(RouteType.REST, requestFacade.getRequestMethod(), requestFacade.getEndpoint(), "")
//                .map(Route::getGroup)
//                .orElse(null);
//        if (group == null) {
//            group = configService
//                    .getEnabledRoute(RouteType.REST, requestFacade.getRequestMethod(), requestFacade.getEndpoint(), "")
//                    .map(Route::getGroup)
//                    .orElseThrow(RouteNotFoundException::new);
//        }

        String path = getPath(requestFacade);
        log.info("File requested: {}", path);
        MockResource resource = resourceCache.get(path);
        Map<String, String> requestVariables = requestFacade.getVariables(variables);
        return ResponseEntity
                .status(resource.getCode())
                .headers(resource.getHeaders())
                .body(resource.getBody(requestVariables));
    }

    private String getPath(RequestFacade requestFacade) {
        return requestFacade.getMethod()
                + "-"
                + requestFacade.getEncodedEndpoint()
                + requestFacade.getSuffix()
                + RouteType.REST.getExt();
    }

    @Override
    public String mockError(Throwable t) {
        try {
            return new ObjectMapper().writeValueAsString(new RestErrorResponse(t));
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
