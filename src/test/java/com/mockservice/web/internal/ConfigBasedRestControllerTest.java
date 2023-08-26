package com.mockservice.web.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.Route;
import com.mockservice.domain.RouteType;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.service.MockService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConfigBasedRestControllerTest {

    private static final String BODY = "{\"id\": 42}";
    private static final RequestMethod METHOD = RequestMethod.PUT;
    private static final String PATH = "/api/v1/test";
    private static final String ALT = "400";

    @Mock
    private HttpServletRequest request;
    @Mock
    private MockService mockService;
    @Mock
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    @Mock
    private ConfigRepository configRepository;
    @Mock
    private ObjectMapper jsonMapper;

    private ConfigBasedRestController controller() {
        try {
            return new ConfigBasedRestController(request, mockService, requestMappingHandlerMapping, configRepository,
                    jsonMapper);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void mock_CompletesSuccessfully_ReturnsResponseEntity() throws ExecutionException, InterruptedException {
        ResponseEntity<String> responseEntity = ResponseEntity.ok().body(BODY);
        when(mockService.mock(any())).thenReturn(responseEntity);

        CompletableFuture<ResponseEntity<String>> mock = controller().mock();

        assertEquals(responseEntity, mock.get());
    }

    @Test
    public void getType_ReturnsRest() {
        assertEquals(RouteType.REST, controller().getType());
    }

    @Test
    public void register_EnabledRouteHasPathAndMethod_RegistersMappingWithThatPathAndMethod() {
        Route route = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route));

        controller();

        verify(requestMappingHandlerMapping, times(1)).registerMapping(any(), any(), any());

        ArgumentCaptor<RequestMappingInfo> argument = ArgumentCaptor.forClass(RequestMappingInfo.class);
        verify(requestMappingHandlerMapping).registerMapping(argument.capture(), any(), any());
        assertTrue(argument.getValue().getMethodsCondition().getMethods().contains(route.getMethod()));
        assertNotNull(argument.getValue().getPathPatternsCondition());
        Set<PathPattern> patterns = argument.getValue().getPathPatternsCondition().getPatterns();
        assertEquals(1, patterns.size());
    }

    @Test
    public void register_TwoEnabledRoutesHaveTheSamePathAndMethod_RegistersOnlyOneMapping() {
        Route route1 = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);
        Route route2 = new Route(route1).setAlt(ALT);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1, route2));

        controller();

        verify(requestMappingHandlerMapping, times(1)).registerMapping(any(), any(), any());
    }

    @Test
    public void register_DisabledRoute_RegistersNoMappings() {
        Route route = new Route().setDisabled(true).setMethod(METHOD).setPath(PATH);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route));

        controller();

        verify(requestMappingHandlerMapping, never()).registerMapping(any(), any(), any());
    }

    @Test
    public void register_RouteTypeNotRest_RegistersNoMappings() {
        Route route = new Route().setDisabled(true).setMethod(METHOD).setPath(PATH).setType(RouteType.SOAP);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route));

        controller();

        verify(requestMappingHandlerMapping, never()).registerMapping(any(), any(), any());
    }

    @Test
    public void register_NoRoutes_RegistersNoMappings() {
        controller();

        verify(requestMappingHandlerMapping, never()).registerMapping(any(), any(), any());
    }

    @Test
    public void onRouteCreated_RouteEnabledAndHasPathAndMethod_RegistersMappingWithThatPathAndMethod() {
        Route route = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);

        controller().onRouteCreated(route);

        verify(requestMappingHandlerMapping, times(1)).registerMapping(any(), any(), any());

        ArgumentCaptor<RequestMappingInfo> argument = ArgumentCaptor.forClass(RequestMappingInfo.class);
        verify(requestMappingHandlerMapping).registerMapping(argument.capture(), any(), any());
        assertTrue(argument.getValue().getMethodsCondition().getMethods().contains(route.getMethod()));
        assertNotNull(argument.getValue().getPathPatternsCondition());
        Set<PathPattern> patterns = argument.getValue().getPathPatternsCondition().getPatterns();
        assertEquals(1, patterns.size());
    }

    @Test
    public void onRouteDeleted_ExistsRegisteredEnabledRoute_UnregistersMappingWithThatPathAndMethod() {
        Route route = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);

        ConfigBasedRestController controller = controller();
        controller.onRouteCreated(route);
        controller.onRouteDeleted(route);

        verify(requestMappingHandlerMapping, times(1)).unregisterMapping(any());

        ArgumentCaptor<RequestMappingInfo> argument = ArgumentCaptor.forClass(RequestMappingInfo.class);
        verify(requestMappingHandlerMapping).unregisterMapping(argument.capture());
        assertTrue(argument.getValue().getMethodsCondition().getMethods().contains(route.getMethod()));
        assertNotNull(argument.getValue().getPathPatternsCondition());
        Set<PathPattern> patterns = argument.getValue().getPathPatternsCondition().getPatterns();
        assertEquals(1, patterns.size());
    }

    @Test
    public void onRouteDeleted_NoRegisteredRoutesWithPathAndMethod_UnregistersNoMappings() {
        Route route = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);

        ConfigBasedRestController controller = controller();
        controller.onRouteDeleted(route);

        verify(requestMappingHandlerMapping, never()).unregisterMapping(any());
    }

    @Test
    public void onRouteDeleted_HasBeenRegisteredMoreThanOneRouteWithSamePathAndMethod_UnregistersNoMappings() {
        Route route1 = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);
        Route route2 = new Route(route1).setAlt(ALT);

        ConfigBasedRestController controller = controller();
        controller.onRouteCreated(route1);
        controller.onRouteCreated(route2);
        controller.onRouteDeleted(route2);

        verify(requestMappingHandlerMapping, never()).unregisterMapping(any());
    }

    @Test
    public void onRouteDeleted_DeleteDisabledRoute_UnregistersNoMappings() {
        Route route1 = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);
        Route route2 = new Route(route1).setDisabled(true).setAlt(ALT);

        ConfigBasedRestController controller = controller();
        controller.onRouteCreated(route1);
        controller.onRouteDeleted(route2);

        verify(requestMappingHandlerMapping, never()).unregisterMapping(any());
    }

    @Test
    public void onRouteDeleted_DeleteRouteTypeNotRest_UnregistersNoMappings() {
        Route route1 = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);
        Route route2 = new Route(route1).setType(RouteType.SOAP);

        ConfigBasedRestController controller = controller();
        controller.onRouteCreated(route1);
        controller.onRouteDeleted(route2);

        verify(requestMappingHandlerMapping, never()).unregisterMapping(any());
    }

    @Test
    public void onBeforeConfigChanged_HasRegisteredEnabledRoutes_UnregistersMappings() {
        Route route = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route));

        ConfigBasedRestController controller = controller();
        controller.onBeforeConfigChanged();

        verify(requestMappingHandlerMapping, times(1)).unregisterMapping(any());
    }

    @Test
    public void onAfterConfigChanged_HasNoRegisteredRoutes_RegistersMappings() {
        ConfigBasedRestController controller = controller();

        Route route = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route));

        controller.onAfterConfigChanged();

        verify(requestMappingHandlerMapping, times(1)).registerMapping(any(), any(), any());
    }

    @Test
    public void handleException_ReturnsBadRequest() {
        ConfigBasedRestController controller = controller();

        ResponseEntity<String> responseEntity = controller.handleException(new Throwable());

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }
}
