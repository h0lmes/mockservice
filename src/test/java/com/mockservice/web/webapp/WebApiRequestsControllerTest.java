package com.mockservice.web.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.model.OutboundRequestDto;
import com.mockservice.service.RequestService;
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
class WebApiRequestsControllerTest {

    private static final String WEB_API_URI = "/web-api/requests";

    private static final String PATH = "/test";

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private RequestService service;

    @Autowired
    @Qualifier("jsonMapper")
    private ObjectMapper jsonMapper;

    @Test
    void getAll() throws Exception {
        OutboundRequestDto dto = new OutboundRequestDto().setPath(PATH);
        when(service.getRequests()).thenReturn(List.of(dto));

        mvc.perform(get(WEB_API_URI).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }

    @Test
    void WHEN_getAndExceptionThrown_THEN_ReturnsBadRequest() throws Exception {
        when(service.getRequests()).thenThrow(RuntimeException.class);

        mvc.perform(get(WEB_API_URI).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchAll() throws Exception {
        OutboundRequestDto empty = new OutboundRequestDto();
        OutboundRequestDto dto = new OutboundRequestDto().setPath(PATH);
        when(service.getRequests()).thenReturn(List.of(dto));

        mvc.perform(patch(WEB_API_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(empty, dto))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }

    @Test
    void putAll() throws Exception {
        OutboundRequestDto dto = new OutboundRequestDto().setPath(PATH);
        when(service.getRequests()).thenReturn(List.of(dto));

        mvc.perform(put(WEB_API_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(dto))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }

    @Test
    void postAll() throws Exception {
        OutboundRequestDto dto = new OutboundRequestDto().setPath(PATH);
        when(service.getRequests()).thenReturn(List.of(dto));

        mvc.perform(post(WEB_API_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(dto))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }

    @Test
    void deleteAll() throws Exception {
        OutboundRequestDto dto = new OutboundRequestDto().setPath(PATH);
        when(service.getRequests()).thenReturn(List.of(dto));

        mvc.perform(delete(WEB_API_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(dto))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }

    @Test
    void execute() throws Exception {
        OutboundRequestDto dto = new OutboundRequestDto().setPath(PATH).setId("id");
        when(service.getRequests()).thenReturn(List.of(dto));

        OutboundRequestExecuteRequest request = new OutboundRequestExecuteRequest();
        request.setRequestId("id");

        mvc.perform(post(WEB_API_URI + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
