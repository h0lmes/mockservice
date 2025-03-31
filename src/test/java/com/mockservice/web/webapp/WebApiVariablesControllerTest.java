package com.mockservice.web.webapp;

import com.mockservice.service.VariablesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@EnableAutoConfiguration()
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebApiVariablesControllerTest {

    private static final String WEB_API_URI = "/web-api/variables";

    private static final String TEXT = "test string";

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private VariablesService service;

    @Test
    void getAll() throws Exception {
        when(service.toString()).thenReturn(TEXT);

        mvc.perform(get(WEB_API_URI))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(TEXT)));
    }

    @Test
    void WHEN_setAndExceptionThrown_THEN_ReturnsBadRequest() throws Exception {
        doThrow(RuntimeException.class).when(service).fromString(any());

        mvc.perform(post(WEB_API_URI).content(TEXT))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void setAll() throws Exception {
        mvc.perform(post(WEB_API_URI).content(TEXT))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
