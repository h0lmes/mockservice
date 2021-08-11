package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.domain.Settings;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.request.RequestFacade;
import com.mockservice.template.TemplateEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class MockServiceImplTest {

    private static final RequestMethod METHOD = RequestMethod.GET;
    private static final String ENDPOINT = "/api/v1/test";

    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private RouteService routeService;
    @Mock
    private ActiveScenariosService activeScenariosService;
    @Mock
    private ConfigRepository configRepository;
    @Mock
    private RequestService requestService;
    @Mock
    private RequestFacade request;

    @BeforeEach
    public void common() {
        when(templateEngine.getFunctions()).thenReturn(new HashMap<>());
        when(activeScenariosService.getAltFor(any(), any())).thenReturn(Optional.empty());
        when(configRepository.getSettings()).thenReturn(new Settings());
    }

    @Test
    public void mock_BodyWithoutVariables_ResponseBodyUnchanged() {
        String bodyWithoutVariables = "[]";
        Route route = new Route().setMethod(METHOD).setPath(ENDPOINT).setResponse(bodyWithoutVariables);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockService mockService = new MockServiceImpl(
                2, templateEngine, routeService, activeScenariosService, configRepository, requestService);
        ResponseEntity<String> responseEntity = mockService.mock(request);

        assertEquals(bodyWithoutVariables, responseEntity.getBody());
    }

    @Test
    public void mock_BodyWithVariables_ResponseBodyVariableSubstitutedWithValue() {
        String variableName = "id";
        String variableValue = "5";
        String bodyWithVariables = "{\"test\": ${" + variableName + "}}";
        String bodyWithVariablesResult = "{\"test\": " + variableValue + "}";
        Route route = new Route().setMethod(METHOD).setPath(ENDPOINT).setResponse(bodyWithVariables);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        Map<String, String> variables = new HashMap<>();
        variables.put(variableName, variableValue);
        when(request.getVariables()).thenReturn(variables);

        MockService mockService = new MockServiceImpl(
                2, templateEngine, routeService, activeScenariosService, configRepository, requestService);
        ResponseEntity<String> responseEntity = mockService.mock(request);

        assertEquals(bodyWithVariablesResult, responseEntity.getBody());
    }

    @Test
    public void mock_BodyWithoutRequest_NoRequestScheduled() {
        String bodyWithoutRequest = "[]";
        Route route = new Route().setMethod(METHOD).setPath(ENDPOINT).setResponse(bodyWithoutRequest);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockService mockService = new MockServiceImpl(
                2, templateEngine, routeService, activeScenariosService, configRepository, requestService);
        mockService.mock(request);

        verify(requestService, never()).schedule(any());
    }

    @Test
    public void mock_BodyWithRequest_RequestScheduled() {
        String bodyWithRequest = "[]\n\nGET http://localhost:8080/ HTTP/1.1";
        Route route = new Route().setMethod(METHOD).setPath(ENDPOINT).setResponse(bodyWithRequest);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockService mockService = new MockServiceImpl(
                2, templateEngine, routeService, activeScenariosService, configRepository, requestService);
        mockService.mock(request);

        verify(requestService, times(1)).schedule(any());
    }
}
