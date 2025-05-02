package com.mockachu.service;

import com.mockachu.components.QuantumTheory;
import com.mockachu.domain.Route;
import com.mockachu.domain.RouteType;
import com.mockachu.domain.Settings;
import com.mockachu.exception.RouteNotFoundException;
import com.mockachu.repository.ConfigRepository;
import com.mockachu.template.MockVariables;
import com.mockachu.validate.DataValidationException;
import com.mockachu.validate.DataValidator;
import com.mockachu.web.mock.MockRequestFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class MockServiceImplTest {

    private static final RequestMethod GET_METHOD = RequestMethod.GET;
    private static final String PATH = "/api/v1/test";
    private static final String ALT_400 = "400";

    private static final String JSON_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "product_id": {"type": "integer"}
              }
            }""";
    private static final String VALID_JSON = "{\"product_id\": 1}";
    private static final String INVALID_JSON = "{\"product_id\": \"\"}";

    private static final String XML_DATA = "<soapenv:Envelope></soapenv:Envelope>";

    @Mock
    private RouteService routeService;
    @Mock
    private ScenarioService scenarioService;
    @Mock
    private ConfigRepository configRepository;
    @Mock
    private RequestService requestService;
    @Mock
    private MockRequestFacade request;
    @Mock
    private QuantumTheory quantumTheory;
    @Mock
    private QuantumTheory quantumTheoryNonApplicable;
    @Mock
    private DataValidator dataValidator;

    private MockService createMockService() {
        return new MockServiceImpl(
                2, routeService, scenarioService, configRepository, requestService,
                List.of(quantumTheoryNonApplicable, quantumTheory), List.of(dataValidator));
    }

    @BeforeEach
    void setup() {
        lenient().when(scenarioService.getAltFor(any(), any())).thenReturn(Optional.empty());
        lenient().when(configRepository.getSettings()).thenReturn(new Settings());
    }

    @Test
    void mock_RouteResponseHasNoVariables_BodyUnchanged() {
        String bodyWithoutVariables = "[]";
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(bodyWithoutVariables);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockService mockService = createMockService();
        ResponseEntity<String> responseEntity = mockService.mock(request);

        assertEquals(bodyWithoutVariables, responseEntity.getBody());
    }

    @Test
    void mock_RouteResponseHasVariable_VariableSubstitutedWithValue() {
        String variableName = "id";
        String variableValue = "5";
        String bodyWithVariables = "{\"test\": ${" + variableName + "}}";
        String bodyWithVariablesResult = "{\"test\": " + variableValue + "}";
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(bodyWithVariables);
        when(routeService.getEnabledRouteType(any(), any())).thenReturn(Optional.of(RouteType.REST));
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockVariables variables = new MockVariables();
        variables.put(variableName, variableValue);
        when(request.getVariables(any(), anyBoolean())).thenReturn(variables);

        MockService mockService = createMockService();
        ResponseEntity<String> responseEntity = mockService.mock(request);

        assertEquals(bodyWithVariablesResult, responseEntity.getBody());
    }

    @Test
    void mock_RouteResponseWithNoTriggerRequest_NoRequestScheduled() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH)
                .setTriggerRequest(false);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockService mockService = createMockService();
        mockService.mock(request);

        verify(requestService, never()).schedule(any(), any(), any());
    }

    @Test
    void mock_RouteResponseWithTriggerRequest_RequestScheduled() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH)
                .setTriggerRequest(true).setTriggerRequestIds("id");
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockService mockService = createMockService();
        mockService.mock(request);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(requestService).schedule(argument.capture(), any(), any());
        assertEquals("id", argument.getValue());
    }

    @Test
    void mock_SettingsRandomAltTrue_RandomAlt400_SearchesRouteWithAlt400() {
        Settings settings = new Settings().setRandomAlt(true);
        when(configRepository.getSettings()).thenReturn(settings);

        when(request.getAlt()).thenReturn(Optional.empty());

        when(routeService.getRandomAltFor(any(), any())).thenReturn(Optional.of(ALT_400));

        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(VALID_JSON);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockService mockService = createMockService();
        mockService.mock(request);

        ArgumentCaptor<Route> argument = ArgumentCaptor.forClass(Route.class);
        verify(routeService).getEnabledRoute(argument.capture());
        assertEquals(ALT_400, argument.getValue().getAlt());
    }

    @Test
    void mock_SoapRoute_ReturnsBody() {
        Route route = new Route().setType(RouteType.SOAP)
                .setMethod(GET_METHOD).setPath(PATH).setResponse(XML_DATA);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockService mockService = createMockService();
        ResponseEntity<String> responseEntity = mockService.mock(request);

        assertEquals(XML_DATA, responseEntity.getBody());
    }

    @Test
    void mock_NoRouteFound_ExceptionThrown() {
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.empty());
        MockService mockService = createMockService();

        assertThrows(RouteNotFoundException.class, () -> mockService.mock(request));
    }

    @Test
    void mock_QuantumEnabled_UsesQuantumToAlterResponse() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(VALID_JSON);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        Settings settings = new Settings().setQuantum(true);
        when(configRepository.getSettings()).thenReturn(settings);

        when(quantumTheoryNonApplicable.applicable(any())).thenReturn(false);
        when(quantumTheory.applicable(any())).thenReturn(true);
        when(quantumTheory.apply(anyString())).thenReturn(INVALID_JSON);
        when(quantumTheory.apply(anyInt())).thenReturn(200);

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
    void cacheRemove_RouteResponseChanged_ReturnNewResponse() {
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

    @Test
    void cacheRemove_RouteExists_LogsEviction(CapturedOutput output) {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(INVALID_JSON);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));
        MockService mockService = createMockService();

        mockService.mock(request);
        mockService.cacheRemove(route);
        assertTrue(output.getOut().contains("evicted"));
    }

    @Test
    void cacheRemove_RouteNotExists_LogsNothing(CapturedOutput output) {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(INVALID_JSON);
        MockService mockService = createMockService();

        mockService.cacheRemove(route);
        assertFalse(output.getOut().contains("evicted"));
    }

    //----------------------------------------------------------------------
    //
    //   JSON validation tests
    //
    //----------------------------------------------------------------------

    @Test
    void mock_RouteHasRequestBodySchema_ValidJson_NoExceptionThrown() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setRequestBodySchema(JSON_SCHEMA);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));
        when(request.getBody()).thenReturn(VALID_JSON);

        MockService mockService = createMockService();
        assertDoesNotThrow(() -> mockService.mock(request));
    }

    @Test
    void mock_RouteHasRequestBodySchema_InvalidJson_Alt400Disabled_ExceptionThrown() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setRequestBodySchema(JSON_SCHEMA);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        Settings settings = new Settings();
        settings.setAlt400OnFailedRequestValidation(false);
        when(configRepository.getSettings()).thenReturn(settings);

        when(dataValidator.applicable(any())).thenReturn(true);
        Mockito.doThrow(DataValidationException.class).when(dataValidator).validate(any(), any());

        MockService mockService = createMockService();
        assertThrows(RuntimeException.class, () -> mockService.mock(request));
    }

    @Test
    void mock_RouteHasRequestBodySchema_InvalidJson_Alt400EnabledButNoRoute400_ExceptionThrown() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setRequestBodySchema(JSON_SCHEMA);
        when(routeService.getEnabledRoute(any()))
                .thenReturn(Optional.of(route))
                .thenReturn(Optional.empty());

        Settings settings = new Settings();
        settings.setAlt400OnFailedRequestValidation(true);
        when(configRepository.getSettings()).thenReturn(settings);

        when(dataValidator.applicable(any())).thenReturn(true);
        Mockito.doThrow(DataValidationException.class).when(dataValidator).validate(any(), any());

        MockService mockService = createMockService();
        assertThrows(RuntimeException.class, () -> mockService.mock(request));
    }

    @Test
    void mock_RouteHasRequestBodySchema_InvalidJson_Alt400EnabledAndRoute400Exists_NoExceptionThrown() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setRequestBodySchema(JSON_SCHEMA);
        Route route400 = new Route(route).setAlt(ALT_400);
        when(routeService.getEnabledRoute(any()))
                .thenReturn(Optional.of(route))
                .thenReturn(Optional.of(route400));

        Settings settings = new Settings();
        settings.setAlt400OnFailedRequestValidation(true);
        when(configRepository.getSettings()).thenReturn(settings);

        when(dataValidator.applicable(any())).thenReturn(true);
        Mockito.doThrow(DataValidationException.class).when(dataValidator).validate(any(), any());

        MockService mockService = createMockService();
        assertDoesNotThrow(() -> mockService.mock(request));
    }
}
