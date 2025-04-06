package com.mockachu.web.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockachu.domain.Scenario;
import com.mockachu.exception.ScenarioAlreadyExistsException;
import com.mockachu.exception.ScenarioParseException;
import com.mockachu.repository.ConfigObserver;
import com.mockachu.repository.RouteObserver;
import com.mockachu.service.ScenarioService;
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
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@EnableAutoConfiguration()
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebApiScenariosControllerTest {

    private static final String WEB_API_SCENARIOS = "/web-api/scenarios";
    private static final String WEB_API_SCENARIOS_ACTIVE = "/web-api/scenarios/active";

    private static final String ALIAS = "alias";

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private ScenarioService scenarioService;

    @MockitoBean
    private List<ConfigObserver> configObservers;
    @MockitoBean
    private List<RouteObserver> routeObservers;

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
        Scenario empty = new Scenario();
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(scenarioService.putScenario(any(), any())).thenReturn(List.of(scenario));

        mvc.perform(
                put(WEB_API_SCENARIOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(empty, scenario)))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].alias", is(ALIAS)));
    }

    @Test
    public void putScenario_ScenarioAlreadyExistsExceptionThrown_BadRequest() throws Exception {
        Scenario empty = new Scenario();
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(scenarioService.putScenario(any(), any())).thenThrow(ScenarioAlreadyExistsException.class);

        mvc.perform(
                put(WEB_API_SCENARIOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(empty, scenario)))
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void putScenario_ScenarioParseExceptionThrown_BadRequest() throws Exception {
        Scenario empty = new Scenario();
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(scenarioService.putScenario(any(), any())).thenThrow(ScenarioParseException.class);

        mvc.perform(
                put(WEB_API_SCENARIOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsBytes(List.of(empty, scenario)))
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
    public void putActiveScenario() throws Exception {
        when(scenarioService.activateScenario(any())).thenReturn(Set.of(ALIAS));

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
        when(scenarioService.deactivateScenario(any())).thenReturn(Set.of(ALIAS));

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
