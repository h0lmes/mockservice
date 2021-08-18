package com.mockservice.web.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.Settings;
import com.mockservice.repository.ConfigRepository;
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

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableAutoConfiguration()
public class WebApiSettingsControllerTest {

    private static final String WEB_API_SETTINGS = "/web-api/settings";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private ConfigRepository configRepository;

    @Autowired
    @Qualifier("jsonMapper")
    private ObjectMapper jsonMapper;

    @Test
    public void getSettings() throws Exception {
        Settings settings = new Settings().setRandomAlt(false).setQuantum(false);
        when(configRepository.getSettings()).thenReturn(settings);

        mvc.perform(
                get(WEB_API_SETTINGS).contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.randomAlt", is(false)))
                .andExpect(jsonPath("$.quantum", is(false)));
    }

    @Test
    public void getSettings_ExceptionThrown_ReturnsBadRequest() throws Exception {
        when(configRepository.getSettings()).thenThrow(RuntimeException.class);

        mvc.perform(
                get(WEB_API_SETTINGS).contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void putSettings() throws Exception {
        Settings settings = new Settings().setRandomAlt(false).setQuantum(false);
        when(configRepository.getSettings()).thenReturn(settings);

        mvc.perform(
                put(WEB_API_SETTINGS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(settings))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.randomAlt", is(false)))
                .andExpect(jsonPath("$.quantum", is(false)));
    }
}
