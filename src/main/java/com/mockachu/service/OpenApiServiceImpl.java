package com.mockachu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.mockachu.components.JsonFromSchemaProducer;
import com.mockachu.domain.Route;
import com.mockachu.domain.RouteType;
import com.mockachu.util.JsonUtils;
import com.mockachu.util.MapUtils;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private final ObjectMapper jsonMapper;
    private final JsonFromSchemaProducer jsonFromSchemaProducer;

    public OpenApiServiceImpl(@Qualifier("jsonMapper") ObjectMapper jsonMapper,
                              JsonFromSchemaProducer jsonFromSchemaProducer) {
        this.jsonMapper = jsonMapper;
        this.jsonFromSchemaProducer = jsonFromSchemaProducer;
    }

    @Override
    public List<Route> routesFromYaml(String yaml) {
        if (yaml == null || yaml.trim().isEmpty()) {
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
        return multiply(routes, paths);
    }

    private List<Route> multiply(List<Route> routes, Set<String> paths) {
        if (paths.isEmpty()) {
            return routes;
        }

        List<Route> result = new ArrayList<>();
        for (String basePath : paths) {
            for (Route route : routes) {
                result.add(new Route(route).setPath(basePath + route.getPath()));
            }
        }
        return result;
    }

    private Set<String> serversFromOpenApi(OpenAPI openApi) {
        Set<String> paths = new HashSet<>();
        List<Server> servers = openApi.getServers();
        Pattern pattern = Pattern.compile(URL_PARTS_REGEX, Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
        for (Server server : servers) {
            Matcher matcher = pattern.matcher(server.getUrl());
            if (matcher.find()) {
                paths.add(matcher.group(3));
            }
        }
        return paths;
    }

    private List<Route> routesFromOpenApi(OpenAPI openApi) {
        List<Route> routes = new ArrayList<>();
        Paths paths = openApi.getPaths();
        Objects.requireNonNull(paths, "routesFromOpenApi(): paths MUST NOT be null!");

        for (Map.Entry<String, PathItem> e : paths.entrySet()) {
            routesFromPath(routes, e.getKey(), e.getValue());
        }
        return routes;
    }

    private void routesFromPath(List<Route> routes, String path, PathItem pathItem) {
        routesFromOperation(routes, path, "GET", pathItem.getGet());
        routesFromOperation(routes, path, "POST", pathItem.getPost());
        routesFromOperation(routes, path, "PUT", pathItem.getPut());
        routesFromOperation(routes, path, "PATCH", pathItem.getPatch());
        routesFromOperation(routes, path, "DELETE", pathItem.getDelete());
    }

    private void routesFromOperation(List<Route> routes, String path, String method, Operation operation) {
        if (operation != null) {
            String group = tagsFromOperation(operation);
            String requestBodySchema = requestBodySchemaFromOperation(operation);
            routesFromResponses(routes, group, path, method, operation.getResponses(), requestBodySchema);
        }
    }

    private String requestBodySchemaFromOperation(Operation operation) {
        Objects.requireNonNull(operation, "requestBodySchemaFromOperation(): operation MUST NOT be null!");

        RequestBody requestBody = operation.getRequestBody();
        if (requestBody != null) {
            MediaType mediaType = mediaTypeFromContent(requestBody.getContent());
            return schemaFromMediaType(mediaType);
        }
        return "";
    }

    private void routesFromResponses(List<Route> routes, String group, String path, String method, ApiResponses responses, String requestBodySchema) {
        if (responses != null) {
            for (Map.Entry<String, ApiResponse> e : responses.entrySet()) {
                routeFromApiResponse(routes, group, path, method, responseCode(e.getKey()), e.getValue(), requestBodySchema);
            }
        }
    }

    private String responseCode(String code) {
        if ("200".equals(code) || "default".equals(code)) {
            return "";
        }
        return code;
    }

    @SuppressWarnings("java:S1643")
    private String tagsFromOperation(Operation operation) {
        String group = "";
        List<String> tags = operation.getTags();
        if (tags != null) {
            String separator = "";
            for (String tag : tags) {
                group += separator + tag; // not using builder since in most cases there will be 1 tag
                separator = ", ";
            }
        }
        return group;
    }

    private void routeFromApiResponse(List<Route> routes,
                                      String group,
                                      String path,
                                      String method,
                                      String responseCode,
                                      ApiResponse apiResponse,
                                      String requestBodySchema) {
        MediaType mediaType = mediaTypeFromContent(apiResponse.getContent());

        String response = exampleFromMediaType(mediaType);
        if (response.isEmpty()) {
            try {
                String jsonSchema = schemaFromMediaType(mediaType);
                Map<String, Object> jsonSchemaMap = jsonMapper.readValue(jsonSchema, Map.class);
                response = jsonFromSchemaProducer.jsonFromSchema(jsonSchemaMap);
            } catch (JsonProcessingException e) {
                //
            }
        }

        routes.add(
                new Route()
                        .setType(RouteType.REST)
                        .setGroup(group)
                        .setMethod(RequestMethod.valueOf(method.toUpperCase()))
                        .setPath(path)
                        .setAlt(responseCode)
                        .setResponseCodeString(responseCode)
                        .setResponse(response)
                        .setRequestBodySchema(requestBodySchema)
        );
    }

    private MediaType mediaTypeFromContent(Content content) {
        if (content != null && !content.isEmpty()) {
            MediaType mediaType = content.get("application/json");
            if (mediaType == null && !content.isEmpty()) {
                mediaType = content.values().iterator().next();
            }
            return mediaType;
        }
        return null;
    }

    private String exampleFromMediaType(MediaType mediaType) {
        if (mediaType != null) {
            Map<String, Example> examples = mediaType.getExamples();
            if (!MapUtils.isEmpty(examples)) {
                Example example1 = examples.values().iterator().next();
                try {
                    return jsonMapper.writeValueAsString(example1.getValue());
                } catch (JsonProcessingException e) {
                    //
                }
            }

            Object example = mediaType.getExample();
            if (example != null) {
                return JsonUtils.unescape(example.toString());
            }
        }
        return "";
    }

    private String schemaFromMediaType(MediaType mediaType) {
        if (mediaType != null) {
            return serializeJsonSchema(jsonMapper, mediaType.getSchema());
        }
        return "";
    }

    private static String serializeJsonSchema(ObjectMapper mapper, Object schema) {
        final String filterName = "exclude-not-in-spec-fields";
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            public Object findFilterId(Annotated a) {
                if (Map.class.isAssignableFrom(a.getRawType())
                        || io.swagger.v3.oas.models.media.Schema.class.isAssignableFrom(a.getRawType())) {
                    return filterName;
                }
                return super.findFilterId(a);
            }
        });
        SimpleFilterProvider filters = new SimpleFilterProvider();
        filters.addFilter(filterName, new SimpleBeanPropertyFilter() {
            @Override
            protected boolean include(BeanPropertyWriter writer) {
                return true;
            }

            @Override
            protected boolean include(PropertyWriter writer) {
                return !writer.getName().equalsIgnoreCase("xml")
                        && !writer.getName().equalsIgnoreCase("exampleSetFlag");
            }
        });
        try {
            return mapper.writer(filters).writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
