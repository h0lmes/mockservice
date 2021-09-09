package com.mockservice.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class HashMapCache<K, V> implements Cache<K, V> {

    private final Map<K, V> cache = new HashMap<>();
    private final Function<K, V> generator;

    public HashMapCache(Function<K, V> generator) {
        this.generator = generator;
    }

    @Override
    public V get(K key) {
        return cache.computeIfAbsent(key, generator);
    }

    @Override
    public void evict(K key) {
        cache.remove(key);
    }

    @Override
    public void invalidate() {
        cache.clear();
    }
}
