package com.mockachu.service;

import com.mockachu.template.MockVariables;
import org.springframework.lang.Nullable;

import java.util.Map;

public interface ContextService {
    void put(String k, String v);
    void putAll(@Nullable Map<String, String> vars);
    void putAll(@Nullable MockVariables vars);
    void putAll(String namespace, @Nullable MockVariables vars);
    MockVariables get();
    void clear();
    String getAsString();
    void setFromString(@Nullable String vars);
}
