package com.mockachu.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ConcurrentHashMapCache<K, V> implements Cache<K, V> {

    private final Map<K, V> cache = new ConcurrentHashMap<>();
    private final Function<K, V> generator;

    public ConcurrentHashMapCache(Function<K, V> generator) {
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
