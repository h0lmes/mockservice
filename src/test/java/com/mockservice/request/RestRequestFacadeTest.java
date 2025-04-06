package com.mockservice.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.template.MockVariables;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestRequestFacadeTest {

    private static final String STR1 = "aaa";
    private static final String STR2 = "bbb";
    private static final String PATH1 = "/" + STR1;
    private static final String PATH2 = "/" + STR2;
    private static final String HEADER_VARIABLE_NAME = "headerVariable";
    private static final String ALT = "400";
    private static final String BODY = "{\"id\": 42}";
    private static final String BODY_INVALID = "{\"id\": ";
    private static final String JWT =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ";
    private static final String JWT_WITH_ONE_CHUNK =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    private static final String JWT_INVALID =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIy";
    private static final String JWT_SUB = "1234567890";

    @Mock
    private HttpServletRequest request;

    private BufferedReader asReader(String str) {
        return new BufferedReader(new StringReader(str));
    }

    @Test
    void getRequestMethod_MethodIsGet_ReturnsGet() {
        when(request.getMethod()).thenReturn("GET");
        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertEquals(RequestMethod.GET, facade.getRequestMethod());
    }

    @Test
    void getEndpoint_Path_ReturnsPath() {
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH1);
        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertEquals(PATH1, facade.getEndpoint());
    }

    @Test
    void getBody_ValidJsonBody_ReturnsBodyJson() throws IOException {
        when(request.getReader()).thenReturn(asReader(BODY));
        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertEquals(BODY, facade.getBody());
    }

    @Test
    void getAlt_MockAltHeaderContainsPathAndAlt_ReturnsAlt() {
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH1);

        Enumeration<String> headers = Collections.enumeration(List.of(STR1 + "/" + ALT));
        lenient().when(request.getHeaders(eq("Mock-Alt"))).thenReturn(headers);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isPresent());
        assertEquals(ALT, facade.getAlt().get());
    }

    @Test
    void getAlt_MockAltHeaderContainsWrongPath_ReturnsEmpty() {
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH1);

        Enumeration<String> headers = Collections.enumeration(List.of(STR2 + "/" + ALT));
        lenient().when(request.getHeaders(eq("Mock-Alt"))).thenReturn(headers);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isEmpty());
    }

    @Test
    void getAlt_MockAltHeaderInvalidFormat_ReturnsEmpty() {
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH1);

        Enumeration<String> headers = Collections.enumeration(List.of(STR1));
        lenient().when(request.getHeaders(eq("Mock-Alt"))).thenReturn(headers);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isEmpty());
    }

    @Test
    void getAlt_MockAltHeaderEmpty_ReturnsEmpty() {
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH1);

        Enumeration<String> headers = Collections.enumeration(List.of(""));
        lenient().when(request.getHeaders(eq("Mock-Alt"))).thenReturn(headers);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isEmpty());
    }

    @Test
    void getAlt_MockAltHeaderIsNull_ReturnsEmpty() {
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH1);

        List<String> list = new ArrayList<>();
        list.add(null);
        Enumeration<String> headers = Collections.enumeration(list);
        lenient().when(request.getHeaders(eq("Mock-Alt"))).thenReturn(headers);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isEmpty());
    }

    @Test
    void getAlt_NoMockAltHeader_ReturnsEmpty() {
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH1);
        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isEmpty());
    }

    @Test
    void getVariables_MultipleSources_ReturnsVariables() throws IOException {
        lenient().when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH1);
        Enumeration<String> headers = Collections.enumeration(List.of(STR1 + "/" + HEADER_VARIABLE_NAME + "/42 42"));
        lenient().when(request.getHeaders(eq("Mock-Variable"))).thenReturn(headers);

        Enumeration<String> authHeaders = Collections.enumeration(List.of("bearer " + JWT));
        lenient().when(request.getHeaders(eq("Authorization"))).thenReturn(authHeaders);

        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("pathVariable", "42");
        lenient().when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathVariables);

        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("parameterVariable", new String[]{"42 42 42"});
        when(request.getParameterMap()).thenReturn(parameterMap);

        when(request.getReader()).thenReturn(asReader(BODY));

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertEquals("42", facade.getVariables(null).get("id"));
        assertEquals("42 42", facade.getVariables(null).get(HEADER_VARIABLE_NAME));
        assertEquals("42", facade.getVariables(null).get("pathVariable"));
        assertEquals("42 42 42", facade.getVariables(null).get("parameterVariable"));
        assertEquals(JWT_SUB, facade.getVariables(null).get("sub"));
    }

    @Test
    void getVariables_HeaderVariableInvalidFormat_NoSuchVariable() {
        lenient().when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH1);
        Enumeration<String> headers = Collections.enumeration(List.of(STR1 + "/" + HEADER_VARIABLE_NAME));
        lenient().when(request.getHeaders(eq("Mock-Variable"))).thenReturn(headers);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());
        MockVariables variables = facade.getVariables(null);

        assertFalse(variables.containsKey(HEADER_VARIABLE_NAME));
    }

    @Test
    void getVariables_HeaderVariableWrongPath_NoSuchVariable() {
        lenient().when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH2);
        Enumeration<String> headers = Collections.enumeration(List.of(STR1 + "/" + HEADER_VARIABLE_NAME + "/42 42"));
        lenient().when(request.getHeaders(eq("Mock-Variable"))).thenReturn(headers);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertFalse(facade.getVariables(null).containsKey(HEADER_VARIABLE_NAME));
    }

    @Test
    void getVariables_InvalidJsonBody_ReturnsNoVariables() throws IOException {
        lenient().when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH1);
        when(request.getReader()).thenReturn(asReader(BODY_INVALID));
        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getVariables(null).isEmpty());
    }

    @Test
    void getVariables_ParameterMapIsNull_DoesNotThrow() {
        when(request.getParameterMap()).thenReturn(null);

        assertDoesNotThrow(() -> new RestRequestFacade(request, new ObjectMapper()));
    }

    @Test
    void getVariables_JwtNotBearer_ReturnsNoVariables() {
        Enumeration<String> authHeaders = Collections.enumeration(List.of(JWT));
        lenient().when(request.getHeaders(eq("Authorization"))).thenReturn(authHeaders);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getVariables(null).isEmpty());
    }

    @Test
    void getVariables_JwtWithOneChunk_ReturnsNoVariables() {
        Enumeration<String> authHeaders = Collections.enumeration(List.of("bearer " + JWT_WITH_ONE_CHUNK));
        lenient().when(request.getHeaders(eq("Authorization"))).thenReturn(authHeaders);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getVariables(null).isEmpty());
    }

    @Test
    void getVariables_JwtInvalid_ReturnsNoVariables() {
        Enumeration<String> authHeaders = Collections.enumeration(List.of("bearer " + JWT_INVALID));
        lenient().when(request.getHeaders(eq("Authorization"))).thenReturn(authHeaders);

        RequestFacade facade = new RestRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getVariables(null).isEmpty());
    }
}
