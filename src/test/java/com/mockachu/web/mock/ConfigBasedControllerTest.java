package com.mockachu.web.mock;

import com.mockachu.domain.Route;
import com.mockachu.domain.Settings;
import com.mockachu.exception.RouteNotFoundException;
import com.mockachu.model.HttpRequestResult;
import com.mockachu.repository.ConfigRepository;
import com.mockachu.service.HttpService;
import com.mockachu.service.MockService;
import com.mockachu.template.MockVariables;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@EnableAutoConfiguration()
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConfigBasedControllerTest {

    private static final String BODY = """
            {"id": 42}""";
    private static final String PATH = "/api/v1/test";
    private static final String ALT = "400";

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private ConfigRepository configRepository;
    @MockitoBean
    private MockService mockService;
    @MockitoBean
    private HttpService httpService;

    @Test
    void mock_CompletesSuccessfully_ReturnsResponseEntity() throws Exception {
        ResponseEntity<String> responseEntity = ResponseEntity.ok().body(BODY);
        when(mockService.mock(any())).thenReturn(responseEntity);

        var route = new Route().setDisabled(false)
                .setMethod(RequestMethod.GET).setPath(PATH).setAlt("");
        when(configRepository.findAllRoutes()).thenReturn(List.of(route));

        var settings = new Settings()
                .setQuantum(false)
                .setRandomAlt(false)
                .setProxyEnabled(false)
                .setProxyLocation("")
                .setAlt400OnFailedRequestValidation(false);
        when(configRepository.getSettings()).thenReturn(settings);

        MvcResult result = mvc.perform(
                        get(PATH).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().string(BODY));
    }

    @Test
    void mock_ThrowsRouteNotFound_ReturnsBadRequest() throws Exception {
        when(mockService.mock(any())).thenThrow(RouteNotFoundException.class);

        when(configRepository.findAllRoutes()).thenReturn(List.of());

        var settings = new Settings()
                .setQuantum(false)
                .setRandomAlt(false)
                .setProxyEnabled(false)
                .setProxyLocation("")
                .setAlt400OnFailedRequestValidation(false);
        when(configRepository.getSettings()).thenReturn(settings);

        MvcResult result = mvc.perform(
                        get(PATH).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    @Test
    void mock_ThrowsAndProxyEnabled_ReturnsResponseEntity() throws Exception {
        when(mockService.mock(any())).thenThrow(RouteNotFoundException.class);
        when(configRepository.findAllRoutes()).thenReturn(List.of());

        var requestResult = new HttpRequestResult(
                false, RequestMethod.GET, PATH, Map.of(), null, "",
                BODY, MockVariables.empty(), 202, 0);
        when(httpService.request(any(), any(), any(), any())).thenReturn(requestResult);

        var settings = new Settings()
                .setQuantum(false)
                .setRandomAlt(false)
                .setProxyEnabled(true)
                .setProxyLocation("")
                .setAlt400OnFailedRequestValidation(false);
        when(configRepository.getSettings()).thenReturn(settings);

        MvcResult result = mvc.perform(
                        get(PATH).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        mvc.perform(asyncDispatch(result))
                .andExpect(status().is(202))
                .andExpect(content().string(BODY));
    }

//    @Test
//    void register_DisabledRoute_RegistersNoMappings() {
//        Route route = new Route().setDisabled(true).setMethod(METHOD).setPath(PATH);
//        when(configRepository.findAllRoutes()).thenReturn(List.of(route));
//
//        controller();
//
//        verify(requestMappingHandlerMapping, never()).registerMapping(any(), any(), any());
//    }
//
//    @Test
//    void register_RouteTypeNotRest_RegistersNoMappings() {
//        Route route = new Route().setDisabled(true).setMethod(METHOD).setPath(PATH).setType(RouteType.SOAP);
//        when(configRepository.findAllRoutes()).thenReturn(List.of(route));
//
//        controller();
//
//        verify(requestMappingHandlerMapping, never()).registerMapping(any(), any(), any());
//    }
//
//    @Test
//    void register_NoRoutes_RegistersNoMappings() {
//        controller();
//        verify(requestMappingHandlerMapping, never()).registerMapping(any(), any(), any());
//    }
//
//    @Test
//    void onRouteCreated_RouteEnabledAndHasPathAndMethod_RegistersMappingWithThatPathAndMethod() {
//        Route route = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);
//
//        controller().onRouteCreated(route);
//
//        verify(requestMappingHandlerMapping, times(1)).registerMapping(any(), any(), any());
//
//        ArgumentCaptor<RequestMappingInfo> argument = ArgumentCaptor.forClass(RequestMappingInfo.class);
//        verify(requestMappingHandlerMapping).registerMapping(argument.capture(), any(), any());
//        assertTrue(argument.getValue().getMethodsCondition().getMethods().contains(route.getMethod()));
//        assertNotNull(argument.getValue().getPathPatternsCondition());
//        Set<PathPattern> patterns = argument.getValue().getPathPatternsCondition().getPatterns();
//        assertEquals(1, patterns.size());
//    }
//
//    @Test
//    void onRouteDeleted_ExistsRegisteredEnabledRoute_UnregistersMappingWithThatPathAndMethod() {
//        Route route = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);
//
//        ConfigBasedController controller = controller();
//        controller.onRouteCreated(route);
//        controller.onRouteDeleted(route);
//
//        verify(requestMappingHandlerMapping, times(1)).unregisterMapping(any());
//
//        ArgumentCaptor<RequestMappingInfo> argument = ArgumentCaptor.forClass(RequestMappingInfo.class);
//        verify(requestMappingHandlerMapping).unregisterMapping(argument.capture());
//        assertTrue(argument.getValue().getMethodsCondition().getMethods().contains(route.getMethod()));
//        assertNotNull(argument.getValue().getPathPatternsCondition());
//        Set<PathPattern> patterns = argument.getValue().getPathPatternsCondition().getPatterns();
//        assertEquals(1, patterns.size());
//    }
//
//    @Test
//    void onRouteDeleted_NoRegisteredRoutesWithPathAndMethod_UnregistersNoMappings() {
//        Route route = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);
//
//        ConfigBasedController controller = controller();
//        controller.onRouteDeleted(route);
//
//        verify(requestMappingHandlerMapping, never()).unregisterMapping(any());
//    }
//
//    @Test
//    void onRouteDeleted_HasBeenRegisteredMoreThanOneRouteWithSamePathAndMethod_UnregistersNoMappings() {
//        Route route1 = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);
//        Route route2 = new Route(route1).setAlt(ALT);
//
//        ConfigBasedController controller = controller();
//        controller.onRouteCreated(route1);
//        controller.onRouteCreated(route2);
//        controller.onRouteDeleted(route2);
//
//        verify(requestMappingHandlerMapping, never()).unregisterMapping(any());
//    }
//
//    @Test
//    void onRouteDeleted_DeleteDisabledRoute_UnregistersNoMappings() {
//        Route route1 = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);
//        Route route2 = new Route(route1).setDisabled(true).setAlt(ALT);
//
//        ConfigBasedController controller = controller();
//        controller.onRouteCreated(route1);
//        controller.onRouteDeleted(route2);
//
//        verify(requestMappingHandlerMapping, never()).unregisterMapping(any());
//    }
//
//    @Test
//    void onRouteDeleted_DeleteRouteTypeNotRest_UnregistersNoMappings() {
//        Route route1 = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);
//        Route route2 = new Route(route1).setType(RouteType.SOAP);
//
//        ConfigBasedController controller = controller();
//        controller.onRouteCreated(route1);
//        controller.onRouteDeleted(route2);
//
//        verify(requestMappingHandlerMapping, never()).unregisterMapping(any());
//    }
//
//    @Test
//    void onBeforeConfigChanged_HasRegisteredEnabledRoutes_UnregistersMappings() {
//        Route route = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);
//        when(configRepository.findAllRoutes()).thenReturn(List.of(route));
//
//        ConfigBasedController controller = controller();
//        controller.onBeforeConfigChanged();
//
//        verify(requestMappingHandlerMapping, times(1)).unregisterMapping(any());
//    }
//
//    @Test
//    void onAfterConfigChanged_HasNoRegisteredRoutes_RegistersMappings() {
//        ConfigBasedController controller = controller();
//
//        Route route = new Route().setDisabled(false).setMethod(METHOD).setPath(PATH);
//        when(configRepository.findAllRoutes()).thenReturn(List.of(route));
//
//        controller.onAfterConfigChanged();
//
//        verify(requestMappingHandlerMapping, times(1)).registerMapping(any(), any(), any());
//    }
//
//    @Test
//    void handleException_ReturnsBadRequest() {
//        ConfigBasedController controller = controller();
//        ResponseEntity<String> responseEntity = controller.handleException(new Throwable());
//        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
//    }
}
