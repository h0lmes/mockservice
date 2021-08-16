package com.mockservice.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class RestRequestFacadeTest {

    private static final String PATH = "/test";
    private static final String ALT = "400";
    private static final String BODY = "{\"id\": 42}";

    @Mock
    private HttpServletRequest request;

    @Test
    public void getRequestMethod_MethodIsGet_ReturnsGet() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertEquals(RequestMethod.GET, facade.getRequestMethod());
    }

    @Test
    public void getEndpoint_Path_ReturnsPath() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertEquals(PATH, facade.getEndpoint());
    }

    @Test
    public void getBody_ValidJsonBody_ReturnsBodyJson() throws IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);

        Reader inputString = new StringReader(BODY);
        BufferedReader reader = new BufferedReader(inputString);
        when(request.getReader()).thenReturn(reader);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertEquals(BODY, facade.getBody());
    }

    @Test
    public void getAlt_MockAltHeaderContainsPathAndAlt_ReturnsAlt() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);

        Enumeration<String> headers = Collections.enumeration(List.of("test/" + ALT));
        lenient().when(request.getHeaders(eq("Mock-Alt"))).thenReturn(headers);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isPresent());
        assertEquals(ALT, facade.getAlt().get());
    }

    @Test
    public void getAlt_MockAltHeaderContainsWrongPath_ReturnsEmpty() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);

        Enumeration<String> headers = Collections.enumeration(List.of("wrong-path/" + ALT));
        lenient().when(request.getHeaders(eq("Mock-Alt"))).thenReturn(headers);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isEmpty());
    }

    @Test
    public void getAlt_MockAltHeaderEmpty_ReturnsEmpty() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);

        Enumeration<String> headers = Collections.enumeration(List.of(""));
        lenient().when(request.getHeaders(eq("Mock-Alt"))).thenReturn(headers);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isEmpty());
    }

    @Test
    public void getAlt_MockAltHeaderIsNull_ReturnsEmpty() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);

        List<String> list = new ArrayList<>();
        list.add(null);
        Enumeration<String> headers = Collections.enumeration(list);
        lenient().when(request.getHeaders(eq("Mock-Alt"))).thenReturn(headers);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isEmpty());
    }

    @Test
    public void getAlt_NoMockAltHeader_ReturnsEmpty() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isEmpty());
    }

    @Test
    public void getVariables_MultipleSources_ReturnsVariables() throws IOException {
        when(request.getMethod()).thenReturn("GET");
        lenient().when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);

        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("pathVariable", "42");
        lenient().when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathVariables);

        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("parameterVariable", new String[]{"42 42 42"});
        when(request.getParameterMap()).thenReturn(parameterMap);

        Enumeration<String> headers = Collections.enumeration(List.of("test/headerVariable/42 42"));
        lenient().when(request.getHeaders(eq("Mock-Variable"))).thenReturn(headers);

        Reader inputString = new StringReader(BODY);
        BufferedReader reader = new BufferedReader(inputString);
        when(request.getReader()).thenReturn(reader);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertEquals("42", facade.getVariables().get("id"));
        assertEquals("42 42", facade.getVariables().get("headerVariable"));
        assertEquals("42", facade.getVariables().get("pathVariable"));
        assertEquals("42 42 42", facade.getVariables().get("parameterVariable"));
    }
}
