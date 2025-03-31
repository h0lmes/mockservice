package com.mockservice.service;

import com.mockservice.template.MockVariables;
import org.springframework.lang.Nullable;

import java.util.Map;

public interface VariablesService {
    void put(String k, String v);
    void putAll(Map<String,String> vars);
    void putAll(MockVariables vars);
    void putAll(String namespace, MockVariables vars);
    MockVariables getAll();
    void clearAll();
    String toString();
    void fromString(@Nullable String vars);
}
