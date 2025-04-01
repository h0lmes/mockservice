package com.mockservice.service;

import com.mockservice.components.QuantumTheory;
import com.mockservice.domain.Route;
import com.mockservice.domain.RouteType;
import com.mockservice.domain.Settings;
import com.mockservice.exception.NoRouteFoundException;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.request.RequestFacade;
import com.mockservice.template.MockFunctions;
import com.mockservice.template.MockVariables;
import com.mockservice.template.TemplateEngine;
import com.mockservice.validate.DataValidationException;
import com.mockservice.validate.DataValidator;
import com.mockservice.ws.WebSocketHandler;
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
public class MockServiceImplTest {

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
    private TemplateEngine templateEngine;
    @Mock
    private RouteService routeService;
    @Mock
    private ScenarioService scenarioService;
    @Mock
    private ConfigRepository configRepository;
    @Mock
    private RequestService requestService;
    @Mock
    private RequestFacade request;
    @Mock
    private QuantumTheory quantumTheory;
    @Mock
    private QuantumTheory quantumTheoryNonApplicable;
    @Mock
    private DataValidator dataValidator;
    @Mock
    private WebSocketHandler webSocketHandler;

    private MockService createMockService() {
        return new MockServiceImpl(
                2, templateEngine, routeService, scenarioService, configRepository, requestService,
                List.of(quantumTheoryNonApplicable, quantumTheory), List.of(dataValidator), webSocketHandler);
    }

    @BeforeEach
    public void setup() {
        lenient().when(templateEngine.getFunctions()).thenReturn(new MockFunctions());
        lenient().when(scenarioService.getAltFor(any(), any())).thenReturn(Optional.empty());
        lenient().when(configRepository.getSettings()).thenReturn(new Settings());
    }

    @Test
    public void mock_RouteResponseHasNoVariables_BodyUnchanged() {
        String bodyWithoutVariables = "[]";
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(bodyWithoutVariables);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockService mockService = createMockService();
        ResponseEntity<String> responseEntity = mockService.mock(request);

        assertEquals(bodyWithoutVariables, responseEntity.getBody());
    }

    @Test
    public void mock_RouteResponseHasVariable_VariableSubstitutedWithValue() {
        String variableName = "id";
        String variableValue = "5";
        String bodyWithVariables = "{\"test\": ${" + variableName + "}}";
        String bodyWithVariablesResult = "{\"test\": " + variableValue + "}";
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(bodyWithVariables);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockVariables variables = new MockVariables();
        variables.put(variableName, variableValue);
        when(request.getVariables(any())).thenReturn(variables);

        MockService mockService = createMockService();
        ResponseEntity<String> responseEntity = mockService.mock(request);

        assertEquals(bodyWithVariablesResult, responseEntity.getBody());
    }

    @Test
    public void mock_RouteResponseWithNoTriggerRequest_NoRequestScheduled() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH)
                .setTriggerRequest(false);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockService mockService = createMockService();
        mockService.mock(request);

        verify(requestService, never()).schedule(any(), any());
    }

    @Test
    public void mock_RouteResponseWithTriggerRequest_RequestScheduled() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH)
                .setTriggerRequest(true).setTriggerRequestIds("id");
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));

        MockService mockService = createMockService();
        mockService.mock(request);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(requestService).schedule(argument.capture(), any());
        assertEquals("id", argument.getValue());
    }

    @Test
    public void mock_SettingsRandomAltTrue_RandomAlt400_SearchesRouteWithAlt400() {
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

    @Test
    public void cacheRemove_RouteExists_LogsEviction(CapturedOutput output) {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setResponse(INVALID_JSON);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));
        MockService mockService = createMockService();

        mockService.mock(request);
        mockService.cacheRemove(route);
        assertTrue(output.getOut().contains("evicted"));
    }

    @Test
    public void cacheRemove_RouteNotExists_LogsNothing(CapturedOutput output) {
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
    public void mock_RouteHasRequestBodySchema_ValidJson_NoExceptionThrown() {
        Route route = new Route().setMethod(GET_METHOD).setPath(PATH).setRequestBodySchema(JSON_SCHEMA);
        when(routeService.getEnabledRoute(any())).thenReturn(Optional.of(route));
        when(request.getBody()).thenReturn(VALID_JSON);

        MockService mockService = createMockService();
        assertDoesNotThrow(() -> mockService.mock(request));
    }

    @Test
    public void mock_RouteHasRequestBodySchema_InvalidJson_Alt400Disabled_ExceptionThrown() {
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
    public void mock_RouteHasRequestBodySchema_InvalidJson_Alt400EnabledButNoRoute400_ExceptionThrown() {
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
    public void mock_RouteHasRequestBodySchema_InvalidJson_Alt400EnabledAndRoute400Exists_NoExceptionThrown() {
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
