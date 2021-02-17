package com.mockservice.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class ResourceService {

    private final ResourceLoader resourceLoader;
    private final Map<String, ResourceWrapper> cache = new LinkedHashMap<>(100);

    public ResourceService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ResourceWrapper getAsWrapper(String path) {
        if (cache.containsKey(path))
            return cache.get(path);

        ResourceWrapper resourceWrapper = new ResourceWrapper(getAsString(path));
        cache.put(path, resourceWrapper);
        return resourceWrapper;
    }

    private String getAsString(String path) {
        Resource resource = resourceLoader.getResource(path);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
