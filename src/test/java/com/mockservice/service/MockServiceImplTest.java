package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.domain.RouteType;
import com.mockservice.domain.Settings;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.request.RequestFacade;
import com.mockservice.service.quantum.QuantumTheory;
import com.mockservice.template.TemplateEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class MockServiceImplTest {

    private static final RequestMethod GET_METHOD = RequestMethod.GET;
    private static final String PATH = "/api/v1/test";
    private static final String ALT_400 = "400";

    private static final String JSON_SCHEMA = "{\"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"product_id\": {\"type\": \"integer\"}\n" +
            "  }}";
    private static final String VALID_JSON = "{\"product_id\": 1}";
    private static final String INVALID_JSON = "{\"product_id\": \"\"}";

    private static final String XML_DATA = "<soapenv:Envelope></soapenv:Envelope>";

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
    @Mock
    private QuantumTheory quantumTheory;

    private MockService createMockService() {
        return new MockServiceImpl(
                2, templateEngine, routeService, activeScenariosService, configRepository, requestService,
                List.of(quantumTheory));
    }

    @BeforeEach
    public void common() {
        lenient().when(templateEngine.getFunctions()).thenReturn(new HashMap<>());
        lenient().when(activeScenariosService.getAltFor(any(), any())).thenReturn(Optional.empty());
        lenient().when(configRepository.getSettings()).thenReturn(new Settings());
    }

    @Test
    public void mock_ResponseBodyHasNoVariables_BodyUnchanged() {
        String bodyWithoutVariables = "[]";
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(bodyWithoutVariables);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockService mockService = createMockService();
        ResponseEntity<String> responseEntity = mockService.mock(request);

        assertEquals(bodyWithoutVariables, responseEntity.getBody());
    }

    @Test
    public void mock_ResponseBodyHasVariable_VariableSubstitutedWithValue() {
        String variableName = "id";
        String variableValue = "5";
        String bodyWithVariables = "{\"test\": ${" + variableName + "}}";
        String bodyWithVariablesResult = "{\"test\": " + variableValue + "}";
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(bodyWithVariables);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        Map<String, String> variables = new HashMap<>();
        variables.put(variableName, variableValue);
        when(request.getVariables()).thenReturn(variables);

        MockService mockService = createMockService();
        ResponseEntity<String> responseEntity = mockService.mock(request);

        assertEquals(bodyWithVariablesResult, responseEntity.getBody());
    }

    @Test
    public void mock_BodyWithNoCallbackRequest_NoRequestScheduled() {
        String bodyWithoutRequest = "[]";
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(bodyWithoutRequest);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockService mockService = createMockService();
        mockService.mock(request);

        verify(requestService, never()).schedule(any());
    }

    @Test
    public void mock_BodyWithCallbackRequest_RequestScheduled() {
        String bodyWithRequest = "[]\n\nGET http://localhost:8080/ HTTP/1.1";
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(bodyWithRequest);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockService mockService = createMockService();
        mockService.mock(request);

        verify(requestService, times(1)).schedule(any());
    }

    @Test
    public void mock_RandomAltEnabled_RandomAltReturned400_SearchesRouteWithAlt400() {
        when(request.getAlt()).thenReturn(Optional.empty());
        when(routeService.getRandomAltFor(any(), any())).thenReturn(Optional.of(ALT_400));
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(VALID_JSON);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        Settings settings = new Settings().setRandomAlt(true);
        when(configRepository.getSettings()).thenReturn(settings);

        MockService mockService = createMockService();
        mockService.mock(request);

        ArgumentCaptor<Route> argument = ArgumentCaptor.forClass(Route.class);
        verify(routeService).getEnabledRoute(argument.capture());
        assertEquals(ALT_400, argument.getValue().getAlt());
    }

    @Test
    public void mock_SoapRoute_ReturnsBody() {
        Route route = new Route().setType(RouteType.SOAP).setMethod(GET_METHOD).setPath(PATH).setResponse(XML_DATA);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockService mockService = createMockService();
        ResponseEntity<String> responseEntity = mockService.mock(request);

        assertEquals(XML_DATA, responseEntity.getBody());
    }

    @Test
    public void mock_NoRouteFound_ExceptionThrown() {
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.empty());
        MockService mockService = createMockService();

        assertThrows(NoRouteFoundException.class, () -> mockService.mock(request));
    }

    @Test
    public void mock_QuantumEnabled_UsesQuantumToAlterResponse() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(VALID_JSON);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        Settings settings = new Settings().setQuantum(true);
        when(configRepository.getSettings()).thenReturn(settings);

        when(quantumTheory.applicable(any())).thenReturn(true);
        when(quantumTheory.apply(any())).thenReturn(INVALID_JSON);

        MockService mockService = createMockService();
        ResponseEntity<String> responseEntity = mockService.mock(request);

        assertEquals(INVALID_JSON, responseEntity.getBody());
    }

    //----------------------------------------------------------------------
    //
    //   cache
    //
    //----------------------------------------------------------------------

    @Test
    public void cacheRemove_RouteResponseChanged_ReturnNewResponse() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(INVALID_JSON);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));
        MockService mockService = createMockService();

        ResponseEntity<String> responseEntity;
        responseEntity = mockService.mock(request);
        assertEquals(INVALID_JSON, responseEntity.getBody());

        route.setResponse(VALID_JSON);
        responseEntity = mockService.mock(request);
        assertEquals(INVALID_JSON, responseEntity.getBody());

        mockService.cacheRemove(route);
        responseEntity = mockService.mock(request);
        assertEquals(VALID_JSON, responseEntity.getBody());
    }

    //----------------------------------------------------------------------
    //
    //   JSON validation tests
    //
    //----------------------------------------------------------------------

    @Test
    public void mock_HasBodySchema_ValidJson_NoExceptionThrown() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setRequestBodySchema(JSON_SCHEMA);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));
        when(request.getBody()).thenReturn(VALID_JSON);

        MockService mockService = createMockService();
        assertDoesNotThrow(() -> mockService.mock(request));
    }

    @Test
    public void mock_HasBodySchema_InvalidJson_Alt400Disabled_ExceptionThrown() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setRequestBodySchema(JSON_SCHEMA);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));
        when(request.getBody()).thenReturn(INVALID_JSON);

        Settings settings = new Settings();
        settings.setAlt400OnFailedRequestValidation(false);
        when(configRepository.getSettings()).thenReturn(settings);

        MockService mockService = createMockService();
        assertThrows(RuntimeException.class, () -> mockService.mock(request));
    }

    @Test
    public void mock_HasBodySchema_InvalidJson_Alt400Enabled_NoRoute400_ExceptionThrown() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setRequestBodySchema(JSON_SCHEMA);
        when(routeService.getEnabledRoute(any()))
                .thenReturn(Optional.of(route))
                .thenReturn(Optional.empty());

        when(request.getBody()).thenReturn(INVALID_JSON);
        when(request.getRequestMethod()).thenReturn(GET_METHOD);
        when(request.getEndpoint()).thenReturn(PATH);
        when(request.getAlt()).thenReturn(Optional.empty());

        Settings settings = new Settings();
        settings.setAlt400OnFailedRequestValidation(true);
        when(configRepository.getSettings()).thenReturn(settings);

        MockService mockService = createMockService();
        assertThrows(RuntimeException.class, () -> mockService.mock(request));
    }

    @Test
    public void mock_HasBodySchema_InvalidJson_Alt400Enabled_Route400Exists_NoExceptionThrown() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setRequestBodySchema(JSON_SCHEMA);
        Route route400 = new Route(route).setAlt(ALT_400);
        when(routeService.getEnabledRoute(any()))
                .thenReturn(Optional.of(route))
                .thenReturn(Optional.of(route400));

        when(request.getBody()).thenReturn(INVALID_JSON);
        when(request.getRequestMethod()).thenReturn(GET_METHOD);
        when(request.getEndpoint()).thenReturn(PATH);
        when(request.getAlt()).thenReturn(Optional.empty());

        Settings settings = new Settings();
        settings.setAlt400OnFailedRequestValidation(true);
        when(configRepository.getSettings()).thenReturn(settings);

        MockService mockService = createMockService();
        assertDoesNotThrow(() -> mockService.mock(request));
    }
}