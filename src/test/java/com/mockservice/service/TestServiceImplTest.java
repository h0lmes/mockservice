package com.mockservice.service;

import com.mockservice.domain.ApiTest;
import com.mockservice.mapper.ApiTestMapperImpl;
import com.mockservice.model.HttpRequestResult;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.template.MockVariables;
import com.mockservice.template.TemplateEngine;
import com.mockservice.ws.WebSocketHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private ConfigRepository configRepository;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private RequestService requestService;
    @Mock
    private HttpService httpService;
    @Mock
    private WebSocketHandler webSocketHandler;

    private VariablesService getVarService() {
        return new VariablesServiceImpl();
    }

    private TestService getService(VariablesService variablesService) {
        return new TestServiceImpl(
                256, configRepository, new ApiTestMapperImpl(), templateEngine,
                variablesService, requestService, httpService, webSocketHandler);
    }

    @Test
    void getTests() {
        var entity = new ApiTest()
                .setAlias("test_alias")
                .setGroup("test_group")
                .setPlan("test_plan");
        when(configRepository.findAllTests()).thenReturn(List.of(entity));
        var varService = getVarService();
        var service = getService(varService);
        var list = service.getTests();

        assertEquals(1, list.size());
        assertEquals("test_alias", list.get(0).getAlias());
        assertEquals("test_group", list.get(0).getGroup());
        assertEquals("test_plan", list.get(0).getPlan());
    }

    @Test
    void execute_VarEval_ValEquals_VarEqualsStrict_Success() {
        var entity = new ApiTest()
                .setAlias("test_alias")
                .setGroup("test_group")
                .setPlan("""
                        id = 1
                        request.id -> 200
                        var == 2
                        var === 2
                        """);
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));

        var requestResult = new HttpRequestResult(
                false, RequestMethod.GET, "uri", Map.of(),
                "body", "response",
                new MockVariables().put("var", "2"),
                200, Instant.now().toEpochMilli());
        when(requestService.executeRequest(anyString(), any(), anyBoolean()))
                .thenReturn(Optional.of(requestResult));

        var varService = getVarService();
        var service = getService(varService);
        var result = service.execute("test_alias", false, false);

        assertEquals(TestRunStatus.OK, result);
        assertEquals("1", varService.getAll().get("id"));
        assertTrue(service.getTestLog("test_alias").contains(TestServiceImpl.SUCCESS));
        assertFalse(service.getTestLog("test_alias").contains(TestServiceImpl.FAILED));
        assertFalse(service.getTestLog("test_alias").contains(TestServiceImpl.WARNING));
        // if test fails check this output
        System.out.println(service.getTestLog("test_alias"));
    }

    @Test
    void execute_ValEquals_Warning() {
        var entity = new ApiTest()
                .setAlias("test_alias")
                .setGroup("test_group")
                .setPlan("""
                        id = 1
                        request.id -> 200
                        var == 1
                        """);
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));

        var requestResult = new HttpRequestResult(
                false, RequestMethod.GET, "uri", Map.of(),
                "body", "response",
                new MockVariables().put("var", "2"),
                200, Instant.now().toEpochMilli());
        when(requestService.executeRequest(anyString(), any(), anyBoolean()))
                .thenReturn(Optional.of(requestResult));

        var varService = getVarService();
        var service = getService(varService);
        service.execute("test_alias", false, false);

        assertFalse(service.getTestLog("test_alias").contains(TestServiceImpl.FAILED));
        assertTrue(service.getTestLog("test_alias").contains(TestServiceImpl.WARNING));
        // if test fails check this output
        System.out.println(service.getTestLog("test_alias"));
    }

    @Test
    void execute_ValEqualsStrict_Failed() {
        var entity = new ApiTest()
                .setAlias("test_alias")
                .setGroup("test_group")
                .setPlan("""
                        id = 1
                        request.id -> 200
                        var === 1
                        """);
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));

        var requestResult = new HttpRequestResult(
                false, RequestMethod.GET, "uri", Map.of(),
                "body", "response",
                new MockVariables().put("var", "2"),
                200, Instant.now().toEpochMilli());
        when(requestService.executeRequest(anyString(), any(), anyBoolean()))
                .thenReturn(Optional.of(requestResult));

        var varService = getVarService();
        var service = getService(varService);
        service.execute("test_alias", false, false);

        assertTrue(service.getTestLog("test_alias").contains(TestServiceImpl.FAILED));
        assertFalse(service.getTestLog("test_alias").contains(TestServiceImpl.WARNING));
        // if test fails check this output
        System.out.println(service.getTestLog("test_alias"));
    }
}
