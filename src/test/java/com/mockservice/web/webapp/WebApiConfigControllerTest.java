package com.mockservice.web.webapp;

import com.mockservice.repository.ConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@EnableAutoConfiguration()
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebApiConfigControllerTest {

    private static final String WEB_API_CONFIG = "/web-api/config";
    private static final String WEB_API_CONFIG_BACKUP = WEB_API_CONFIG + "/backup";
    private static final String WEB_API_CONFIG_RESTORE = WEB_API_CONFIG + "/restore";

    private static final String STR_1 = "line 1";

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private ConfigRepository configRepository;

    @Test
    public void getConfig() throws Exception {
        when(configRepository.getConfigData()).thenReturn(STR_1);

        MvcResult result = mvc.perform(
                get(WEB_API_CONFIG).contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(STR_1, result.getResponse().getContentAsString());
    }

    @Test
    public void getConfig_ExceptionThrown_ReturnsBadRequest() throws Exception {
        when(configRepository.getConfigData()).thenThrow(RuntimeException.class);

        mvc.perform(
                get(WEB_API_CONFIG).contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void putConfig() throws Exception {
        mvc.perform(
                put(WEB_API_CONFIG)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(STR_1)
        )
                .andDo(print())
                .andExpect(status().isOk());

        verify(configRepository, times(1)).writeConfigData(STR_1);
    }

    @Test
    public void backup() throws Exception {
        mvc.perform(
                get(WEB_API_CONFIG_BACKUP)
        )
                .andDo(print())
                .andExpect(status().isOk());

        verify(configRepository, times(1)).backup();
    }

    @Test
    public void restore() throws Exception {
        mvc.perform(
                get(WEB_API_CONFIG_RESTORE)
        )
                .andDo(print())
                .andExpect(status().isOk());

        verify(configRepository, times(1)).restore();
    }
}
