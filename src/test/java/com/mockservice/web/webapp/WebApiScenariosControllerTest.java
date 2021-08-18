package com.mockservice.web.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.Route;
import com.mockservice.domain.Scenario;
import com.mockservice.domain.ScenarioAlreadyExistsException;
import com.mockservice.domain.ScenarioParseException;
import com.mockservice.service.ActiveScenariosService;
import com.mockservice.service.RouteService;
import com.mockservice.service.ScenarioService;
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

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableAutoConfiguration()
public class WebApiScenariosControllerTest {

    private static final String WEB_API_SCENARIOS = "/web-api/scenarios";
    private static final String WEB_API_SCENARIOS_ACTIVE = "/web-api/scenarios/active";

    private static final String ALIAS = "alias";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private ScenarioService scenarioService;
    @MockBean
    private ActiveScenariosService activeScenariosService;

    @Autowired
    @Qualifier("jsonMapper")
    private ObjectMapper jsonMapper;

    @Test
    public void getScenarios() throws Exception {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(scenarioService.getScenariosAsList()).thenReturn(List.of(scenario));

        mvc.perform(
                get(WEB_API_SCENARIOS).contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].alias", is(ALIAS)));
    }

    @Test
    public void getScenarios_ExceptionThrown_ReturnsBadRequest() throws Exception {
        when(scenarioService.getScenariosAsList()).thenThrow(RuntimeException.class);

        mvc.perform(
                get(WEB_API_SCENARIOS).contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void putScenario() throws Exception {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(scenarioService.putScenario(any(), any())).thenReturn(List.of(scenario));

        mvc.perform(
                put(WEB_API_SCENARIOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(scenario, scenario)))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].alias", is(ALIAS)));
    }

    @Test
    public void putScenario_NotExactly2Scenarios_BadRequest() throws Exception {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(scenarioService.putScenario(any(), any())).thenReturn(List.of(scenario));

        mvc.perform(
                put(WEB_API_SCENARIOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(scenario)))
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void putScenario_ScenarioAlreadyExistsExceptionThrown_BadRequest() throws Exception {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(scenarioService.putScenario(any(), any())).thenThrow(ScenarioAlreadyExistsException.class);

        mvc.perform(
                put(WEB_API_SCENARIOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(scenario)))
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void putScenario_ScenarioParseExceptionThrown_BadRequest() throws Exception {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(scenarioService.putScenario(any(), any())).thenThrow(ScenarioParseException.class);

        mvc.perform(
                put(WEB_API_SCENARIOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(scenario)))
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteScenario() throws Exception {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(scenarioService.deleteScenario(any())).thenReturn(List.of(scenario));

        mvc.perform(
                delete(WEB_API_SCENARIOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(scenario))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].alias", is(ALIAS)));
    }

    @Test
    public void getActiveScenarios() throws Exception {
        when(activeScenariosService.getActiveScenarios()).thenReturn(Set.of(ALIAS));

        mvc.perform(
                get(WEB_API_SCENARIOS_ACTIVE).contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0]", is(ALIAS)));
    }

    @Test
    public void putActiveScenario() throws Exception {
        when(activeScenariosService.activateScenario(any())).thenReturn(Set.of(ALIAS));

        mvc.perform(
                put(WEB_API_SCENARIOS_ACTIVE)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(ALIAS)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0]", is(ALIAS)));
    }

    @Test
    public void deleteActiveScenario() throws Exception {
        when(activeScenariosService.deactivateScenario(any())).thenReturn(Set.of(ALIAS));

        mvc.perform(
                delete(WEB_API_SCENARIOS_ACTIVE)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(ALIAS)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0]", is(ALIAS)));
    }
}
