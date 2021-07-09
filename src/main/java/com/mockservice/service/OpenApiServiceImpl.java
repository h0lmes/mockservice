package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mockservice.domain.Route;
import com.mockservice.domain.RouteType;
import com.mockservice.util.JsonUtil;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
@Service
public class OpenApiServiceImpl implements OpenApiService {

    private static final Logger log = LoggerFactory.getLogger(OpenApiServiceImpl.class);

    private static final String URL_PARTS_REGEX = "(https?:\\/\\/)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\b(\\/.*)";
    private final ObjectWriter jsonWriter;

    public OpenApiServiceImpl(YamlMapperService yamlMapperService) {
        jsonWriter = yamlMapperService.jsonWriter();
    }

    @Override
    public List<Route> routesFromYaml(String yaml) {
        if (yaml == null || yaml.isEmpty()) {
            return new ArrayList<>();
        }

        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setResolveFully(true);
        SwaggerParseResult result = new OpenAPIParser().readContents(yaml, null, parseOptions);
        if (result.getMessages() != null) {
            result.getMessages().forEach(log::warn);
        }
        OpenAPI openApi = result.getOpenAPI();
        if (openApi == null) {
            return new ArrayList<>();
        }

        List<Route> routes = routesFromOpenApi(openApi);
        Set<String> paths = serversFromOpenApi(openApi);
        if (paths.isEmpty()) {
            return routes;
        }
        return multiply(routes, paths);
    }

    private List<Route> multiply(List<Route> routes, Set<String> paths) {
        List<Route> result = new ArrayList<>();
        for (String basePath : paths) {
            for (Route route : routes) {
                String path = basePath + route.getPath();
                result.add(new Route(route).setPath(path));
            }
        }
        return result;
    }

    private Set<String> serversFromOpenApi(OpenAPI openApi) {
        Set<String> paths = new HashSet<>();
        List<Server> servers = openApi.getServers();
        if (servers != null) {
            Pattern pattern = Pattern.compile(URL_PARTS_REGEX, Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
            for (Server server : servers) {
                Matcher matcher = pattern.matcher(server.getUrl());
                if (matcher.find() && matcher.groupCount() >= 3) {
                    paths.add(matcher.group(3));
                }
            }
        }
        return paths;
    }

    private List<Route> routesFromOpenApi(OpenAPI openApi) {
        List<Route> routes = new ArrayList<>();
        Paths paths = openApi.getPaths();
        if (paths != null) {
            for (Map.Entry<String, PathItem> e : paths.entrySet()) {
                routesFromPath(routes, e.getKey(), e.getValue());
            }
        }
        return routes;
    }

    private void routesFromPath(List<Route> routes, String path, PathItem pathItem) {
        routesFromOperation(routes, path, "GET", pathItem.getGet());
        routesFromOperation(routes, path, "POST", pathItem.getPost());
        routesFromOperation(routes, path, "PUT", pathItem.getPut());
        routesFromOperation(routes, path, "DELETE", pathItem.getDelete());
        routesFromOperation(routes, path, "PATCH", pathItem.getPatch());
    }

    private void routesFromOperation(List<Route> routes, String path, String method, Operation operation) {
        if (operation != null) {
            String group = tagsFromOperation(operation);
            routesFromResponses(routes, group, path, method, operation);
        }
    }

    private String tagsFromOperation(Operation operation) {
        String group = "";
        List<String> tags = operation.getTags();
        if (tags != null) {
            String separator = "";
            for (String tag : tags) {
                group += separator + tag; // not using builder since in most cases there will be 0 or 1 tag
                separator = ", ";
            }
        }
        return group;
    }

    private void routesFromResponses(List<Route> routes,
                                     String group,
                                     String path,
                                     String method,
                                     Operation operation) {
        ApiResponses responses = operation.getResponses();
        if (responses != null) {
            for (Map.Entry<String, ApiResponse> e : responses.entrySet()) {
                String responseCode = e.getKey();
                if ("200".equals(responseCode) || "default".equals(responseCode)) {
                    responseCode = "";
                }

                Content content = e.getValue().getContent();

                routes.add(new Route()
                        .setDisabled(false)
                        .setType(RouteType.REST)
                        .setPath(path)
                        .setMethod(RequestMethod.valueOf(method.toUpperCase()))
                        .setGroup(group)
                        .setAlt(responseCode)
                        .setResponse(exampleFromContent(content))
                        .setResponseSchema(schemaFromContent(content))
                );
            }
        }
    }

    private String exampleFromContent(Content content) {
        if (content != null && !content.isEmpty()) {
            MediaType mediaType = content.get("application/json");
            if (mediaType == null) {
                mediaType = content.get(content.keySet().iterator().next());
            }
            if (mediaType != null) {
                Object example = mediaType.getExample();
                if (example != null) {
                    return JsonUtil.unescape(example.toString());
                }
            }
        }
        return "";
    }

    private String schemaFromContent(Content content) {
        if (content != null && !content.isEmpty()) {
            MediaType mediaType = content.get("application/json");
            if (mediaType == null) {
                mediaType = content.get(content.keySet().iterator().next());
            }
            if (mediaType != null) {
                Schema schema = mediaType.getSchema();
                try {
                    return jsonWriter.writeValueAsString(schema);
                } catch (JsonProcessingException e) {
                    return "";
                }
            }
        }
        return "";
    }
}
