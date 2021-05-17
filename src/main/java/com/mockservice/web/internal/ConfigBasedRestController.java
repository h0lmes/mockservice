package com.mockservice.web.internal;

import com.mockservice.service.ConfigService;
import com.mockservice.mockconfig.RouteType;
import com.mockservice.service.MockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

@RestController
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

        configService.getActiveRoutes()
                .filter(route -> RouteType.REST.equals(route.getType()))
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
    protected ResponseEntity<ErrorInfo> handleException(Throwable t, WebRequest request) {
        log.error("", t);

        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(t));
    }

    private static class ErrorInfo {

        private String type;
        private String message;

        public ErrorInfo(Throwable t) {
            this(t.getClass().getSimpleName(), t.getMessage());
        }

        public ErrorInfo(String type, String message) {
            this.type = type;
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }
    }
}
