package com.mockservice.web.webapp;

import com.mockservice.producer.JsonFromSchemaProducer;
import com.mockservice.producer.JsonProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
public class WebApiGenerateControllerTest {

    private static final String WEB_API_GENERATE_JSON = "/web-api/generate/json";

    private static final String JSON = "{\"test\": 1}";
    private static final String YAML = "test: 1";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private JsonProducer jsonProducer;
    @MockBean
    private JsonFromSchemaProducer jsonFromSchemaProducer;

    @Test
    public void json() throws Exception {
        when(jsonProducer.generate()).thenReturn(JSON);

        MvcResult result = mvc.perform(
                get(WEB_API_GENERATE_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(JSON, result.getResponse().getContentAsString());
    }

    @Test
    public void json_ExceptionThrown_ReturnsBadRequest() throws Exception {
        when(jsonProducer.generate()).thenThrow(RuntimeException.class);

        mvc.perform(
                get(WEB_API_GENERATE_JSON)
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void jsonFromSchema_FromJson() throws Exception {
        when(jsonFromSchemaProducer.jsonFromSchema(any())).thenReturn(JSON);

        MvcResult result = mvc.perform(
                post(WEB_API_GENERATE_JSON)
                        .content(JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(JSON, result.getResponse().getContentAsString());
    }

    @Test
    public void jsonFromSchema_FromYaml() throws Exception {
        when(jsonFromSchemaProducer.jsonFromSchema(any())).thenReturn(JSON);

        MvcResult result = mvc.perform(
                post(WEB_API_GENERATE_JSON)
                        .content(YAML)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(JSON, result.getResponse().getContentAsString());
    }
}
