package com.mockservice.service;

import com.fasterxml.jackson.databind.ObjectReader;
import com.mockservice.domain.Route;
import com.mockservice.domain.RouteType;
import com.mockservice.util.JsonUtil;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
@Service
@Primary
public class OpenApiServiceImpl implements OpenApiService {

    private static final String URL_PARTS_REGEX = "(https?:\\/\\/)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\b(\\/.*)";
    private final ObjectReader yamlReader;

    public OpenApiServiceImpl(YamlMapperService yamlMapperService) {
        this.yamlReader = yamlMapperService.reader();
    }

    @Override
    public List<Route> routesFromYaml(String yaml) throws IOException {
        if (yaml == null || yaml.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, Object> map = yamlReader.readValue(yaml, Map.class);

        Set<String> paths;
        if (isOpenApi3Map(map)) {
            paths = serversFromMap(map);
        } else if (isSwagger2Map(map)) {
            paths = basePathFromMap(map);
        } else {
            throw new UnsupportedOperationException("Unsupported input format. Expected OpenAPI 3 or Swagger 2.");
        }

        List<Route> routes = routesFromMap(map);
        if (paths.isEmpty()) {
            return routes;
        }
        return multiply(routes, paths);
    }

    private boolean isOpenApi3Map(Map<String, Object> map) {
        Object o = map.get("openapi");
        return (o instanceof String) && o.toString().startsWith("3");
    }

    private boolean isSwagger2Map(Map<String, Object> map) {
        Object o = map.get("swagger");
        return (o instanceof String) && o.toString().startsWith("2");
    }

    private List<Route> multiply(List<Route> routes, Set<String> paths) {
        List<Route> result = new ArrayList<>();
        for (String basePath : paths) {
            for (Route route : routes) {
                String path = basePath + route.getPath();
                result.add(route.clone().setPath(path));
            }
        }
        return result;
    }

    private Set<String> serversFromMap(Map<String, Object> map) {
        Set<String> paths = new HashSet<>();
        Object servers = map.get("servers");
        if (servers instanceof List) {
            Pattern pattern = Pattern.compile(URL_PARTS_REGEX, Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
            for (Map<String, String> server : (List<Map<String, String>>) servers) {
                Matcher matcher = pattern.matcher(server.get("url"));
                if (matcher.find() && matcher.groupCount() >= 3) {
                    paths.add(matcher.group(3));
                }
            }
        }
        return paths;
    }

    private Set<String> basePathFromMap(Map<String, Object> map) {
        Set<String> paths = new HashSet<>();
        Object basePath = map.get("basePath");
        if (basePath instanceof String) {
            paths.add(basePath.toString());
        }
        return paths;
    }

    private List<Route> routesFromMap(Map<String, Object> map) {
        Object paths = map.get("paths");

        List<Route> routes = new ArrayList<>();
        if (paths instanceof Map) {
            for (Map.Entry<String, Object> e : ((Map<String, Object>) paths).entrySet()) {
                routesFromPath(routes, e.getKey(), e.getValue(), map);
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
                group += separator + tag.toString(); // not using builder since in most cases there will be 0 or 1 tag
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
