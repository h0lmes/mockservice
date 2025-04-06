package com.mockachu.web.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockachu.model.RouteDto;
import com.mockachu.service.RouteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@EnableAutoConfiguration()
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebApiRoutesControllerTest {

    private static final String WEB_API_ROUTES = "/web-api/routes";

    private static final String PATH = "/test";

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private RouteService service;

    @Autowired
    @Qualifier("jsonMapper")
    private ObjectMapper jsonMapper;

    @Test
    public void getRoutes() throws Exception {
        RouteDto dto = new RouteDto().setPath(PATH);
        when(service.getRoutes()).thenReturn(List.of(dto));

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
        when(service.getRoutes()).thenThrow(RuntimeException.class);

        mvc.perform(
                get(WEB_API_ROUTES).contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void patchRoutes() throws Exception {
        RouteDto empty = new RouteDto();
        RouteDto dto = new RouteDto().setPath(PATH);
        when(service.getRoutes()).thenReturn(List.of(dto));

        mvc.perform(
                patch(WEB_API_ROUTES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(empty, dto)))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }

    @Test
    public void putRoutes() throws Exception {
        RouteDto dto = new RouteDto().setPath(PATH);
        when(service.getRoutes()).thenReturn(List.of(dto));

        mvc.perform(
                put(WEB_API_ROUTES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(dto)))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }

    @Test
    public void postRoutes() throws Exception {
        RouteDto dto = new RouteDto().setPath(PATH);
        when(service.getRoutes()).thenReturn(List.of(dto));

        mvc.perform(
                post(WEB_API_ROUTES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(dto)))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }

    @Test
    public void deleteRoutes() throws Exception {
        RouteDto dto = new RouteDto().setPath(PATH);
        when(service.getRoutes()).thenReturn(List.of(dto));

        mvc.perform(
                delete(WEB_API_ROUTES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(dto)))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }
}
