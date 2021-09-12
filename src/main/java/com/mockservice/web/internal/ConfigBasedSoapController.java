package com.mockservice.web.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.Route;
import com.mockservice.domain.RouteType;
import com.mockservice.repository.ConfigObserver;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.repository.RouteObserver;
import com.mockservice.request.RequestFacade;
import com.mockservice.request.SoapRequestFacade;
import com.mockservice.service.MockService;
import com.mockservice.util.IOUtils;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@RestController
public class ConfigBasedSoapController implements RouteRegisteringController, ConfigObserver, RouteObserver {

    private static final Logger log = LoggerFactory.getLogger(ConfigBasedSoapController.class);

    private static final String FAULT_CODE_PLACEHOLDER = "${code}";
    private static final String FAULT_MESSAGE_PLACEHOLDER = "${message}";

    private final HttpServletRequest request;
    private final MockService mockService;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ConfigRepository configRepository;
    private final Method mockMethod;
    private final ObjectMapper jsonMapper;
    private final Map<String, Integer> registeredRoutes = new ConcurrentHashMap<>();
    private String errorBody;

    public ConfigBasedSoapController(@Value("${application.soap-error-data-file}") String soapErrorDataFile,
                                     HttpServletRequest request,
                                     MockService mockService,
                                     RequestMappingHandlerMapping requestMappingHandlerMapping,
                                     ConfigRepository configRepository,
                                     @Qualifier("jsonMapper") ObjectMapper jsonMapper) throws NoSuchMethodException {
        this.request = request;
        this.mockService = mockService;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.configRepository = configRepository;
        this.jsonMapper = jsonMapper;

        mockMethod = this.getClass().getMethod("mock");

        try {
            errorBody = IOUtils.asString(soapErrorDataFile);
        } catch (IOException e) {
            log.error("Error loading SOAP error data file, using fallback.", e);
            errorBody = "<code>" + FAULT_CODE_PLACEHOLDER + "</code>\n<message>" + FAULT_MESSAGE_PLACEHOLDER + "</message>";
        }

        register();
    }

    public CompletableFuture<ResponseEntity<String>> mock() {
        RequestFacade facade = new SoapRequestFacade(request, jsonMapper);
        return CompletableFuture.supplyAsync(() -> mockService.mock(facade));
    }

    @Override
    public RouteType getType() {
        return RouteType.SOAP;
    }

    private void register() {
        configRepository.findAllRoutes().forEach(this::registerRoute);
    }

    @Override
    public void onBeforeConfigChanged() {
        configRepository.findAllRoutes().forEach(this::unregisterRoute);
    }

    @Override
    public void onAfterConfigChanged() {
        configRepository.findAllRoutes().forEach(this::registerRoute);
    }

    @Override
    public void onRouteCreated(Route route) {
        registerRoute(route);
    }

    @Override
    public void onRouteDeleted(Route route) {
        unregisterRoute(route);
    }

    private void registerRoute(Route route) {
        this.registerRouteInt(route, registeredRoutes, mockMethod, requestMappingHandlerMapping, log);
    }

    private void unregisterRoute(Route route) {
        this.unregisterRouteInt(route, registeredRoutes, requestMappingHandlerMapping, mockService, log);
    }

    @ExceptionHandler
    protected ResponseEntity<String> handleException(Throwable t) {
        log.error("", t);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mockError(t));
    }

    private String mockError(Throwable t) {
        return errorBody
                .replace(FAULT_CODE_PLACEHOLDER, t.getClass().getSimpleName())
                .replace(FAULT_MESSAGE_PLACEHOLDER, t.getMessage());
    }
}
