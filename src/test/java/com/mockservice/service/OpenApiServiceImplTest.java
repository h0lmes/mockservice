package com.mockservice.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mockservice.domain.Route;
import com.mockservice.util.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class OpenApiServiceImplTest {

    private static final String PATH = "/v1/test";

    private OpenApiService createOpenApiService() {
        return new OpenApiServiceImpl(getJsonMapper());
    }

    private ObjectMapper getJsonMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @Test
    public void routesFromYaml() throws IOException {
        OpenApiService openApiService = createOpenApiService();
        String yaml = IOUtils.asString("openapi_test.yml");

        List<Route> routes = openApiService.routesFromYaml(yaml);

        assertEquals(5, routes.size());
        assertTrue(routes.stream()
                        .anyMatch(route -> RequestMethod.GET.equals(route.getMethod())
                                && PATH.equals(route.getPath())
                                && "".equals(route.getAlt())
                        ),
                "GET"
        );
        assertTrue(routes.stream()
                        .anyMatch(route -> RequestMethod.POST.equals(route.getMethod())
                                && PATH.equals(route.getPath())
                                && "".equals(route.getAlt())
                        ),
                "POST"
        );
        assertTrue(routes.stream()
                        .anyMatch(route -> RequestMethod.POST.equals(route.getMethod())
                                && PATH.equals(route.getPath())
                                && "204".equals(route.getAlt())
                        ),
                "POST 204"
        );
        assertTrue(routes.stream()
                        .anyMatch(route -> RequestMethod.POST.equals(route.getMethod())
                                && PATH.equals(route.getPath())
                                && "400".equals(route.getAlt())
                        ),
                "POST 400"
        );
        assertTrue(routes.stream()
                        .anyMatch(route -> RequestMethod.POST.equals(route.getMethod())
                                && PATH.equals(route.getPath())
                                && "404".equals(route.getAlt())
                        ),
                "POST 404"
        );
    }

    @Test
    public void routesFromYaml_NullYaml_ReturnsNoRoutes() throws IOException {
        OpenApiService openApiService = createOpenApiService();

        List<Route> routes = openApiService.routesFromYaml(null);

        assertTrue(routes.isEmpty());
    }

    @Test
    public void routesFromYaml_EmptyYaml_ReturnsNoRoutes() throws IOException {
        OpenApiService openApiService = createOpenApiService();

        List<Route> routes = openApiService.routesFromYaml("");

        assertTrue(routes.isEmpty());
    }
}
