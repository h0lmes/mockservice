package com.mockachu.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockachu.template.MockVariables;
import com.mockachu.util.IOUtils;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SoapRequestFacadeTest {

    private static final String PATH = "/test";
    private static final String ALT = "400";
    private static final String BODY = "<id>42</id>";

    @Mock
    private HttpServletRequest request;

    private BufferedReader asReader(String str) {
        return new BufferedReader(new StringReader(str));
    }

    @Test
    void getRequestMethod_MethodIsGet_ReturnsGet() {
        when(request.getMethod()).thenReturn("POST");
        RequestFacade facade = new SoapRequestFacade(request, new ObjectMapper());

        assertEquals(RequestMethod.POST, facade.getRequestMethod());
    }

    @Test
    void getEndpoint_Path_ReturnsPath() {
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);
        RequestFacade facade = new SoapRequestFacade(request, new ObjectMapper());

        assertEquals(PATH, facade.getEndpoint());
    }

    @Test
    void getBody_ValidBody_ReturnsBody() throws IOException {
        when(request.getReader()).thenReturn(asReader(BODY));
        RequestFacade facade = new SoapRequestFacade(request, new ObjectMapper());

        assertEquals(BODY, facade.getBody());
    }

    @Test
    void getAlt_MockAltHeaderContainsPathAndAlt_ReturnsAlt() {
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);

        Enumeration<String> headers = Collections.enumeration(List.of("test/" + ALT));
        lenient().when(request.getHeaders(eq("Mock-Alt"))).thenReturn(headers);

        RequestFacade facade = new SoapRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isPresent());
        assertEquals(ALT, facade.getAlt().get());
    }

    @Test
    void getAlt_MockAltHeaderContainsWrongPath_ReturnsEmpty() {
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);

        Enumeration<String> headers = Collections.enumeration(List.of("wrong-path/" + ALT));
        lenient().when(request.getHeaders(eq("Mock-Alt"))).thenReturn(headers);

        RequestFacade facade = new SoapRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isEmpty());
    }

    @Test
    void getAlt_MockAltHeaderEmpty_ReturnsEmpty() {
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);

        Enumeration<String> headers = Collections.enumeration(List.of(""));
        lenient().when(request.getHeaders(eq("Mock-Alt"))).thenReturn(headers);

        RequestFacade facade = new SoapRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isEmpty());
    }

    @Test
    void getAlt_MockAltHeaderIsNull_ReturnsEmpty() {
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);

        List<String> list = new ArrayList<>();
        list.add(null);
        Enumeration<String> headers = Collections.enumeration(list);
        lenient().when(request.getHeaders(eq("Mock-Alt"))).thenReturn(headers);

        RequestFacade facade = new SoapRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isEmpty());
    }

    @Test
    void getAlt_NoMockAltHeader_ReturnsEmpty() {
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);
        RequestFacade facade = new SoapRequestFacade(request, new ObjectMapper());

        assertTrue(facade.getAlt().isEmpty());
    }

    @Test
    void getVariables_MultipleSources_ReturnsVariables() throws IOException {
        lenient().when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(PATH);
        Enumeration<String> headers = Collections.enumeration(List.of("test/headerVariable/42 42"));
        lenient().when(request.getHeaders(eq("Mock-Variable"))).thenReturn(headers);

        String body = IOUtils.asString("soap_envelope_valid.xml");
        when(request.getReader()).thenReturn(asReader(body));

        RequestFacade facade = new SoapRequestFacade(request, new ObjectMapper());
        MockVariables variables = facade.getVariables(null);

        assertEquals("${NumberToDollarsRequest.Value:DEFAULT_VALUE}",
                variables.get("NumberToDollarsResponse.Result"));
        assertEquals("42 42", variables.get("headerVariable"));
    }

    @Test
    void getVariables_InvalidXmlInBody_ReturnsNoVariables() throws IOException {
        String body = IOUtils.asString("soap_envelope_invalid.xml");
        when(request.getReader()).thenReturn(asReader(body));

        RequestFacade facade = new SoapRequestFacade(request, new ObjectMapper());
        MockVariables variables = facade.getVariables(null);

        assertTrue(variables.isEmpty());
    }

    @Test
    void getVariables_EmptyBody_ReturnsNoVariables() throws IOException {
        when(request.getReader()).thenReturn(asReader(""));
        RequestFacade facade = new SoapRequestFacade(request, new ObjectMapper());
        MockVariables variables = facade.getVariables(null);

        assertTrue(variables.isEmpty());
    }
}
