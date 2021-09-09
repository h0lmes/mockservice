package com.mockservice.util;

public interface Cache<K, V> {
    V get(K key);
    void evict(K key);
    void invalidate();
}
