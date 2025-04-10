package com.mockachu.web.webapi;

import com.mockachu.components.JsonFromSchemaProducer;
import com.mockachu.components.JsonProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@EnableAutoConfiguration()
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GenerateControllerTest {
    private static final String WEB_API_GENERATE_JSON = "/__webapi__/generate/json";
    private static final String JSON = "{\"test\": 1}";
    private static final String YAML = "test: 1";

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private JsonProducer jsonProducer;
    @MockitoBean
    private JsonFromSchemaProducer jsonFromSchemaProducer;

    @Test
    void json() throws Exception {
        when(jsonProducer.generate()).thenReturn(JSON);

        MvcResult result = mvc.perform(get(WEB_API_GENERATE_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(JSON, result.getResponse().getContentAsString());
    }

    @Test
    void json_ExceptionThrown_ReturnsBadRequest() throws Exception {
        when(jsonProducer.generate()).thenThrow(RuntimeException.class);

        mvc.perform(get(WEB_API_GENERATE_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void jsonFromSchema_FromJson() throws Exception {
        when(jsonFromSchemaProducer.jsonFromSchema(any())).thenReturn(JSON);

        MvcResult result = mvc.perform(post(WEB_API_GENERATE_JSON).content(JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(JSON, result.getResponse().getContentAsString());
    }

    @Test
    void jsonFromSchema_FromYaml() throws Exception {
        when(jsonFromSchemaProducer.jsonFromSchema(any())).thenReturn(JSON);

        MvcResult result = mvc.perform(post(WEB_API_GENERATE_JSON).content(YAML))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(JSON, result.getResponse().getContentAsString());
    }
}
