package com.mockservice.service;

import com.mockservice.template.MockVariables;
import com.mockservice.util.IOUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VariablesServiceImpl implements VariablesService {
    private static final String VAR_DELIMITER = "=";
    private static final int VAR_DELIMITER_LEN = VAR_DELIMITER.length();

    private final MockVariables variables = new MockVariables();

    @Override
    public void put(String k, String v) {
        variables.put(k, v);
    }

    @Override
    public void putAll(@Nullable Map<String, String> vars) {
        variables.putAll(vars);
    }

    @Override
    public void putAll(@Nullable MockVariables vars) {
        variables.putAll(vars);
    }

    @Override
    public void putAll(String namespace, @Nullable MockVariables vars) {
        if (vars != null) {
            vars.forEach((k, v) -> variables.put(namespace + "." + k, v));
        }
    }

    @Override
    public MockVariables getAll() {
        return variables;
    }

    @Override
    public void clearAll() {
        variables.clear();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        variables.forEachSorted((k, v) -> {
            if (!builder.isEmpty()) builder.append('\n');
            builder.append(k).append(" = ").append(v);
        });
        return builder.toString();
    }

    @Override
    public void fromString(@Nullable String vars) {
        if (vars == null || vars.isEmpty()) {
            clearAll();
            return;
        }

        Map<String,String> map = new HashMap<>();
        List<String> lines = IOUtils.toList(vars);
        for (String line : lines) {
            if (!line.trim().isEmpty() && line.contains(VAR_DELIMITER)) {
                int delimiter = line.indexOf(VAR_DELIMITER);
                String key = line.substring(0, delimiter).trim();
                String value = line.substring(delimiter + VAR_DELIMITER_LEN).trim();
                map.put(key, value);
            }
        }
        clearAll();
        putAll(map);
    }
}
