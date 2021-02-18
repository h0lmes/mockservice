package com.mockservice.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class ResourceService {

    private static final int CACHE_SIZE = 256;
    private final ResourceLoader resourceLoader;
    private final ConcurrentLruCache<String, ResourceWrapper> cache;

    public ResourceService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        cache = new ConcurrentLruCache<>(CACHE_SIZE, path -> new ResourceWrapper(getAsString(path)));
    }

    public ResourceWrapper getAsWrapper(String path) {
        return cache.get(path);
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
