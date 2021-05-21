package com.mockservice.web.internal;

import com.mockservice.mockconfig.RouteType;
import com.mockservice.service.ConfigService;
import com.mockservice.service.MockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

@Component
public class ConfigBasedRestController {

    private static final Logger log = LoggerFactory.getLogger(ConfigBasedRestController.class);

    private final MockService mockService;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ConfigService configService;

    public ConfigBasedRestController(@Qualifier("rest") MockService mockService,
                                     RequestMappingHandlerMapping requestMappingHandlerMapping,
                                     ConfigService configService) {
        this.mockService = mockService;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.configService = configService;

        try {
            register();
        } catch (Exception e) {
            log.error("Failed to register configured routes.", e);
        }
    }

    private void register() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("mock");

        configService
                .getRoutesDistinctByPathAndMethod(RouteType.REST)
                .forEach(route -> {
                    RequestMappingInfo mappingInfo = RequestMappingInfo
                            .paths(route.getPath())
                            .methods(route.getMethod())
                            .build();
                    requestMappingHandlerMapping.registerMapping(mappingInfo, this, method);
                });
    }

    public ResponseEntity<String> mock() {
        return mockService.mock(null);
    }

    @ExceptionHandler
    protected ResponseEntity<String> handleException(Throwable t) {
        log.error("", t);
        return ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mockService.mockError(t));
    }
}
