package com.mockservice.service;

import com.fasterxml.jackson.databind.ObjectReader;
import com.mockservice.domain.Route;
import com.mockservice.domain.RouteType;
import com.mockservice.util.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
@Service
public class OpenApi3Service implements OpenApiService {

    private final ObjectReader yamlReader;

    public OpenApi3Service(YamlMapperService yamlMapperService) {
        this.yamlReader = yamlMapperService.reader();
    }

    @Override
    public List<Route> routesFromYaml(String yaml) throws IOException {
        Map<String, Object> map = yamlReader.readValue(yaml, Map.class);
        return routesFromOpenApiMap(map);
    }

    private List<Route> routesFromOpenApiMap(Map<String, Object> map) {
        String basePath = (String) map.get("basePath");
        Object paths = map.get("paths");

        List<Route> routes = new ArrayList<>();
        if (paths instanceof Map) {
            for (Map.Entry<String, Object> e : ((Map<String, Object>) paths).entrySet()) {
                routesFromPath(routes, basePath + e.getKey(), e.getValue(), map);
            }
        }
        return routes;
    }

    private void routesFromPath(List<Route> routes, String path, Object methods, Map<String, Object> map) {
        if (methods instanceof Map) {
            for (Map.Entry<String, Object> e : ((Map<String, Object>) methods).entrySet()) {
                String method = e.getKey();
                routesFromMethod(routes, path, method, e.getValue(), map);
            }
        }
    }

    private void routesFromMethod(List<Route> routes, String path, String method, Object methodDefinition, Map<String, Object> map) {
        if (methodDefinition instanceof Map) {
            String group = tagsFromMethodDefinition((Map<String, Object>) methodDefinition);
            routesFromResponses(routes, group, path, method, (Map<String, Object>) methodDefinition, map);
        }
    }

    private String tagsFromMethodDefinition(Map<String, Object> methodDefinition) {
        String group = "";
        Object tags = methodDefinition.get("tags");
        if (tags instanceof List) {
            String separator = "";
            for (Object tag : (List) tags) {
                group += separator + tag.toString(); // not using builder as in most cases there will be 0 or 1 tag
                separator = ", ";
            }
        }
        return group;
    }

    private void routesFromResponses(List<Route> routes,
                                     String group,
                                     String path,
                                     String method,
                                     Map<String, Object> methodDefinition,
                                     Map<String, Object> map) {
        Object responses = methodDefinition.get("responses");
        if (responses instanceof Map) {
            for (Map.Entry<String, Object> e : ((Map<String, Object>) responses).entrySet()) {
                String responseCode = e.getKey();
                if ("200".equals(responseCode) || "default".equals(responseCode)) {
                    responseCode = "";
                }

                String example = "";
                Object response = e.getValue();
                if (response instanceof Map) {
                    Object schema = ((Map<String, Object>) response).get("schema");
                    example = exampleFromSchema(schema, map);
                }

                routes.add(new Route()
                        .setDisabled(false)
                        .setType(RouteType.REST)
                        .setPath(path)
                        .setMethod(RequestMethod.valueOf(method.toUpperCase()))
                        .setGroup(group)
                        .setAlt(responseCode)
                        .setResponse(example)
                );
            }
        }
    }

    private String exampleFromSchema(Object schema, Map<String, Object> map) {
        if (schema instanceof Map) {
            Object example = ((Map<String, Object>) schema).get("example");
            if (example != null) {
                return JsonUtil.unescape(example.toString());
            }
        }
        return "";
    }
}
