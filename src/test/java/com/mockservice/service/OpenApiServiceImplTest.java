package com.mockservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.Route;
import com.mockservice.util.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class OpenApiServiceImplTest {

    private OpenApiService createOpenApiService() {
        return new OpenApiServiceImpl(getJsonMapper());
    }

    private ObjectMapper getJsonMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }

    @Test
    public void routesFromYaml() throws IOException {
        OpenApiService openApiService = createOpenApiService();
        String yaml = IOUtils.asString("openapi_test.yml");

        List<Route> routes = openApiService.routesFromYaml(yaml);

        assertEquals(1, routes.size());
    }
}
