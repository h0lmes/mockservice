package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.request.HttpServletRequestFacade;
import com.mockservice.template.TemplateConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class MockService {

    @Autowired
    private HttpServletRequest request;

    private static final int CACHE_SIZE = 256;
    private final ResourceLoader resourceLoader;
    private final ConcurrentLruCache<String, MockResource> resourceCache;

    public MockService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        resourceCache = new ConcurrentLruCache<>(CACHE_SIZE, this::loadResource);
    }

    private MockResource loadResource(String path) {
        return new MockResource(loadResourceString(path));
    }

    private String loadResourceString(String path) {
        Resource resource = resourceLoader.getResource(path);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ResponseEntity<String> mock(String folder, Map<String, String> variables, boolean useBodyAsVariables) {
        HttpServletRequestFacade requestFacade = new HttpServletRequestFacade(request, folder);
        MockResource resource = resourceCache.get(requestFacade.getPath());
        requestFacade.mockTimeout();
        Map<String, String> requestVariables = requestFacade.getVariables(variables, useBodyAsVariables);
        String body = resource.getBody(requestVariables, makeSuppliers());

        return ResponseEntity.status(resource.getCode()).headers(resource.getHeaders()).body(body);
    }

    public <T> ResponseEntity<T> mock(String folder, Map<String, String> variables, boolean useBodyAsVariables, Class<T> clazz) throws JsonProcessingException {
        HttpServletRequestFacade requestFacade = new HttpServletRequestFacade(request, folder);
        MockResource resource = resourceCache.get(requestFacade.getPath());
        requestFacade.mockTimeout();
        Map<String, String> requestVariables = requestFacade.getVariables(variables, useBodyAsVariables);
        String jsonBody = resource.getBody(requestVariables, makeSuppliers());

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructType(clazz);
        T body = mapper.readValue(jsonBody, type);

        return ResponseEntity.status(resource.getCode()).headers(resource.getHeaders()).body(body);
    }

    private Map<String, Supplier<String>> makeSuppliers() {
        Map<String, Supplier<String>> map = new HashMap<>();
        for (TemplateConstants constant : TemplateConstants.values()) {
            map.put(constant.getName(), constant.getSupplier());
        }
        return map;
    }
}
