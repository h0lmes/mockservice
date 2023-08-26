package com.mockservice.web.webapp;

import com.mockservice.domain.Route;
import com.mockservice.service.OpenApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@EnableAutoConfiguration()
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebApiImportControllerTest {

    private static final String WEB_API_IMPORT = "/web-api/import";

    private static final String JSON = "{\"test\": 1}";
    private static final String PATH = "/test";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private OpenApiService openApiService;

    @Test
    public void json() throws Exception {
        Route route = new Route().setPath(PATH);
        when(openApiService.routesFromYaml(any())).thenReturn(List.of(route));

        mvc.perform(
                put(WEB_API_IMPORT).content("")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].path", is(PATH)));
    }

    @Test
    public void json_ExceptionThrown_ReturnsBadRequest() throws Exception {
        when(openApiService.routesFromYaml(any())).thenThrow(RuntimeException.class);

        mvc.perform(
                put(WEB_API_IMPORT).content("")
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
