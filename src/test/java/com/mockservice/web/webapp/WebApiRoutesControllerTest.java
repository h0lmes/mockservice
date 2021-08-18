package com.mockservice.web.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.Route;
import com.mockservice.service.RouteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableAutoConfiguration()
public class WebApiRoutesControllerTest {

    private static final String WEB_API_ROUTES = "/web-api/routes";

    private static final String PATH = "/test";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private RouteService service;

    @Autowired
    @Qualifier("jsonMapper")
    private ObjectMapper jsonMapper;

    @Test
    public void getRoutes() throws Exception {
        Route route = new Route().setPath(PATH);
        when(service.getRoutesAsList()).thenReturn(List.of(route));

        mvc.perform(
                get(WEB_API_ROUTES).contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }

    @Test
    public void getRoutes_ExceptionThrown_ReturnsBadRequest() throws Exception {
        when(service.getRoutesAsList()).thenThrow(RuntimeException.class);

        mvc.perform(
                get(WEB_API_ROUTES).contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void patchRoutes() throws Exception {
        Route route = new Route().setPath(PATH);
        when(service.putRoute(any())).thenReturn(List.of(route));

        mvc.perform(
                patch(WEB_API_ROUTES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(route))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }

    @Test
    public void putRoutes() throws Exception {
        Route route = new Route().setPath(PATH);
        when(service.putRoutes(anyList(), anyBoolean())).thenReturn(List.of(route));

        mvc.perform(
                put(WEB_API_ROUTES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(route)))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }

    @Test
    public void postRoutes() throws Exception {
        Route route = new Route().setPath(PATH);
        when(service.putRoutes(anyList(), anyBoolean())).thenReturn(List.of(route));

        mvc.perform(
                post(WEB_API_ROUTES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(route)))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }

    @Test
    public void deleteRoutes() throws Exception {
        Route route = new Route().setPath(PATH);
        when(service.deleteRoutes(anyList())).thenReturn(List.of(route));

        mvc.perform(
                delete(WEB_API_ROUTES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(route)))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }
}
