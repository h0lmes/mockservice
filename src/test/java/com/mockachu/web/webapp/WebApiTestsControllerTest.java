package com.mockachu.web.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockachu.model.ApiTestDto;
import com.mockachu.service.TestRunStatus;
import com.mockachu.service.TestService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@EnableAutoConfiguration()
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebApiTestsControllerTest {

    private static final String WEB_API_URI = "/web-api/tests";

    private static final String ALIAS = "test string";

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private TestService service;

    @Autowired
    @Qualifier("jsonMapper")
    private ObjectMapper jsonMapper;

    @Test
    void getAll() throws Exception {
        ApiTestDto dto = new ApiTestDto().setAlias(ALIAS);
        when(service.getTests()).thenReturn(List.of(dto));

        mvc.perform(get(WEB_API_URI).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].alias", is(ALIAS)));
    }

    @Test
    void WHEN_getAndExceptionThrown_THEN_ReturnsBadRequest() throws Exception {
        when(service.getTests()).thenThrow(RuntimeException.class);

        mvc.perform(get(WEB_API_URI).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchAll() throws Exception {
        ApiTestDto empty = new ApiTestDto();
        ApiTestDto dto = new ApiTestDto().setAlias(ALIAS);
        when(service.getTests()).thenReturn(List.of(dto));

        mvc.perform(patch(WEB_API_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(empty, dto))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].alias", is(ALIAS)));
    }

    @Test
    void putAll() throws Exception {
        ApiTestDto dto = new ApiTestDto().setAlias(ALIAS);
        when(service.getTests()).thenReturn(List.of(dto));

        mvc.perform(put(WEB_API_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(dto))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].alias", is(ALIAS)));
    }

    @Test
    void postAll() throws Exception {
        ApiTestDto dto = new ApiTestDto().setAlias(ALIAS);
        when(service.getTests()).thenReturn(List.of(dto));

        mvc.perform(post(WEB_API_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(dto))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].alias", is(ALIAS)));
    }

    @Test
    void deleteAll() throws Exception {
        ApiTestDto dto = new ApiTestDto().setAlias(ALIAS);
        when(service.getTests()).thenReturn(List.of(dto));

        mvc.perform(delete(WEB_API_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(dto))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].alias", is(ALIAS)));
    }

    @Test
    void execute() throws Exception {
        when(service.execute(any(), anyBoolean(), anyBoolean())).thenReturn(TestRunStatus.OK);

        mvc.perform(post(WEB_API_URI + "/execute")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(ALIAS))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void execute202() throws Exception {
        when(service.execute(any(), anyBoolean(), anyBoolean())).thenReturn(TestRunStatus.ALREADY_IN_PROGRESS);

        mvc.perform(post(WEB_API_URI + "/execute")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(ALIAS))
                .andDo(print())
                .andExpect(status().isAccepted());
    }

    @Test
    void execute404() throws Exception {
        when(service.execute(any(), anyBoolean(), anyBoolean())).thenReturn(TestRunStatus.NOT_FOUND);

        mvc.perform(post(WEB_API_URI + "/execute")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(ALIAS))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void stop() throws Exception {
        when(service.stop(any())).thenReturn(TestRunStatus.OK);

        mvc.perform(post(WEB_API_URI + "/stop")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(ALIAS))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void stop404() throws Exception {
        when(service.stop(any())).thenReturn(TestRunStatus.NOT_FOUND);

        mvc.perform(post(WEB_API_URI + "/stop")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(ALIAS))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void result() throws Exception {
        when(service.getTestLog(any())).thenReturn("result");

        mvc.perform(get(WEB_API_URI + "/" + ALIAS + "/result")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(ALIAS))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("result")));
    }

    @Test
    void clear() throws Exception {
        when(service.clearTestLog(any())).thenReturn(TestRunStatus.OK);

        mvc.perform(post(WEB_API_URI + "/" + ALIAS + "/clear")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(ALIAS))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void clear202() throws Exception {
        when(service.clearTestLog(any())).thenReturn(TestRunStatus.ALREADY_IN_PROGRESS);

        mvc.perform(post(WEB_API_URI + "/" + ALIAS + "/clear")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(ALIAS))
                .andDo(print())
                .andExpect(status().isAccepted());
    }

    @Test
    void clear404() throws Exception {
        when(service.clearTestLog(any())).thenReturn(TestRunStatus.NOT_FOUND);

        mvc.perform(post(WEB_API_URI + "/" + ALIAS + "/clear")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(ALIAS))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
