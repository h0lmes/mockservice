package com.mockservice.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mockservice.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class JsonHttpRequestFacade extends HttpRequestFacade {

    private static final String JSON_FILE_EXTENSION = ".json";

    public JsonHttpRequestFacade(@NonNull HttpServletRequest request,
                                 @NonNull String folder) {
        super(request, folder);
    }

    @Override
    public String getPath() {
        return "classpath:"
                + getFolder()
                + PATH_DELIMITER
                + getRequest().getMethod().toUpperCase()
                + PATH_DELIMITER_SUBSTITUTE
                + getEncodedEndpoint()
                + getMockOption()
                + JSON_FILE_EXTENSION;
    }

    @Override
    public Map<String, String> getVariables(@Nullable Map<String, String> variables) {
        Map<String, String> vars = new HashMap<>();
        vars.putAll(getBodyAsVariables());
        vars.putAll(getPathVariables());
        vars.putAll(getRequestParams());
        vars.putAll(getMockVariables());
        if (variables != null) {
            vars.putAll(variables);
        }
        return vars;
    }

    private Map<String, String> getBodyAsVariables() {
        String body = getBody();
        if (body != null && !body.isEmpty()) {
            try {
                return jsonToFlatMap(body);
            } catch (JsonProcessingException e) {
                // Not a valid JSON. Ignore.
            }
        }
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> jsonToFlatMap(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        Map<String, Object> map = mapper.readValue(json, Map.class);
        return flattenMap(map);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> flattenMap(Map<String, Object> map) {
        Queue<Pair<String, Pair<String, Object>>> q = new ArrayDeque<>();
        Map<String, String> result = new HashMap<>();

        map.forEach((k, v) -> q.offer(new Pair<>(null, new Pair<>(k, v))));

        while (!q.isEmpty()) {
            Pair<String, Pair<String, Object>> el = q.poll();
            String key = el.getKey() == null ? el.getValue().getKey() : el.getKey() + "." + el.getValue().getKey();
            Object obj = el.getValue().getValue();
            if (obj instanceof Map) {
                ((Map<String, Object>) obj).forEach((k, v) -> q.offer(new Pair<>(key, new Pair<>(k, v))));
            } else {
                result.put(key, obj == null ? "null" : obj.toString());
            }
        }
        return result;
    }
}
