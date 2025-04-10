package com.mockachu.web.webapi;

import com.mockachu.domain.Route;
import com.mockachu.service.OpenApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@EnableAutoConfiguration()
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ImportControllerTest {
    private static final String WEB_API_IMPORT = "/__webapi__/import";
    private static final String PATH = "/test";

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private OpenApiService openApiService;

    @Test
    void json() throws Exception {
        Route route = new Route().setPath(PATH);
        when(openApiService.routesFromYaml(any())).thenReturn(List.of(route));
        mvc.perform(put(WEB_API_IMPORT).content(""))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }

    @Test
    void json_ExceptionThrown_ReturnsBadRequest() throws Exception {
        when(openApiService.routesFromYaml(any())).thenThrow(RuntimeException.class);
        mvc.perform(put(WEB_API_IMPORT).content(""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
