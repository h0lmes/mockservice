package com.mockservice.service;

import com.mockservice.template.MockVariables;
import org.springframework.lang.Nullable;

import java.util.Map;

public interface VariablesService {
    void put(String k, String v);
    void putAll(@Nullable Map<String, String> vars);
    void putAll(@Nullable MockVariables vars);
    void putAll(String namespace, @Nullable MockVariables vars);
    MockVariables getAll();
    void clearAll();
    String toString();
    void fromString(@Nullable String vars);
}
