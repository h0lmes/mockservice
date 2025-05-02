package com.mockachu.service;

import com.mockachu.domain.ApiTest;
import com.mockachu.mapper.ApiTestMapperImpl;
import com.mockachu.model.ApiTestDto;
import com.mockachu.model.HttpRequestResult;
import com.mockachu.repository.ConfigRepository;
import com.mockachu.template.MockVariables;
import com.mockachu.ws.WebSocketHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private ConfigRepository configRepository;
    @Mock
    private RequestService requestService;
    @Mock
    private HttpService httpService;
    @Mock
    private WebSocketHandler webSocketHandler;
    @Mock
    private ContextService contextService;

    private ContextService getContextService() {
        return new ContextServiceImpl(configRepository);
    }

    private TestService getService(ContextService contextService) {
        return new TestServiceImpl(
                256, configRepository, new ApiTestMapperImpl(),
                contextService, requestService, httpService, webSocketHandler);
    }

    //---------------------------------------------------------------------
    //
    // repo operations
    //
    //---------------------------------------------------------------------

    @Test
    void WHEN_getTests_THEN_findsAllTests() {
        var entity = new ApiTest().setAlias("alias").setGroup("group").setPlan("plan");
        when(configRepository.findAllTests()).thenReturn(List.of(entity));
        var contextService = getContextService();
        var service = getService(contextService);
        var list = service.getTests();

        assertEquals(1, list.size());
        assertEquals("alias", list.get(0).getAlias());
        assertEquals("group", list.get(0).getGroup());
        assertEquals("plan", list.get(0).getPlan());
    }

    @Test
    void putTest_DtoAsInput_CallsRepositoryMethod() throws IOException {
        var contextService = getContextService();
        var service = getService(contextService);
        service.putTest(new ApiTestDto(), new ApiTestDto());

        verify(configRepository, times(1)).putTest(any(), any());
    }

    @Test
    void putTests_ListOfDtoAsInput_CallsRepositoryMethod() throws IOException {
        var contextService = getContextService();
        var service = getService(contextService);
        service.putTests(List.of(new ApiTestDto()), true);

        verify(configRepository, times(1)).putTests(anyList(), anyBoolean());
    }

    @SuppressWarnings("unchecked")
    @Test
    void deleteTests_ListOfDtoAsInput_CallsRepositoryMethod() throws IOException {
        var contextService = getContextService();
        var service = getService(contextService);
        service.deleteTests(List.of(new ApiTestDto().setAlias("alias")));

        ArgumentCaptor<List<ApiTest>> captor = ArgumentCaptor.forClass(List.class);
        verify(configRepository).deleteTests(captor.capture());
        assertFalse(captor.getValue().isEmpty());
        assertEquals("alias", captor.getValue().get(0).getAlias());
    }

    //---------------------------------------------------------------------
    //
    // execution
    //
    //---------------------------------------------------------------------

    @Test
    void WHEN_testNotFoundByAlias_THEN_notFound() {
        when(configRepository.findTest(anyString())).thenReturn(Optional.empty());

        var contextService = getContextService();
        var service = getService(contextService);

        var exec = service.execute("test_alias", false, false);
        assertEquals(TestRunStatus.NOT_FOUND, exec);

        var stop = service.stop("test_alias");
        assertEquals(TestRunStatus.NOT_FOUND, stop);

        assertTrue(service.getTestLog("test_alias").toLowerCase().contains("not found"));

        var clear = service.clearTestLog("test_alias");
        assertEquals(TestRunStatus.NOT_FOUND, clear);
    }

    @Test
    void WHEN_testFoundByAliasAndNotRunYet_THEN_notFoundAndNotRun() {
        var entity = new ApiTest().setAlias("alias").setGroup("group").setPlan("");
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));

        var contextService = getContextService();
        var service = getService(contextService);

        var stop = service.stop("alias");
        assertEquals(TestRunStatus.NOT_FOUND, stop);

        assertTrue(service.getTestLog("alias").toLowerCase().contains("not run"));

        var clear = service.clearTestLog("alias");
        assertEquals(TestRunStatus.NOT_FOUND, clear);
    }

    @Test
    void WHEN_emptyPlan_THEN_Success() {
        var entity = new ApiTest().setAlias("alias").setGroup("group").setPlan("");
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));

        var contextService = getContextService();
        var service = getService(contextService);
        var result = service.execute("alias", false, false);

        System.out.println(service.getTestLog("alias"));
        assertEquals(TestRunStatus.OK, result);
        assertTrue(service.getTestLog("alias").contains(TestServiceImpl.SUCCESS));
        assertFalse(service.getTestLog("alias").contains(TestServiceImpl.WARNING));
        assertFalse(service.getTestLog("alias").contains(TestServiceImpl.FAILED));
    }

    @Test
    void WHEN_testVariableAndCorrectValue_THEN_Success() {
        var entity = new ApiTest().setAlias("alias").setGroup("group")
                .setPlan("""
                        request.id
                        x == 2
                        x === 2
                        """);
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));

        var requestResult = new HttpRequestResult(
                false, RequestMethod.GET, "uri", Map.of(), null,
                "body", "response",
                new MockVariables().put("x", "2"),
                200, Instant.now().toEpochMilli());
        when(requestService.executeRequest(anyString(), any(), anyBoolean()))
                .thenReturn(Optional.of(requestResult));

        var contextService = getContextService();
        var service = getService(contextService);
        var result = service.execute("alias", false, false);

        System.out.println(service.getTestLog("alias"));
        assertEquals(TestRunStatus.OK, result);
        assertTrue(service.getTestLog("alias").contains(TestServiceImpl.SUCCESS));
        assertFalse(service.getTestLog("alias").contains(TestServiceImpl.WARNING));
        assertFalse(service.getTestLog("alias").contains(TestServiceImpl.FAILED));
    }

    @Test
    void WHEN_testVariableAndIncorrectValue_THEN_Warning() {
        var entity = new ApiTest().setAlias("alias").setGroup("group")
                .setPlan("""
                        request.id
                        x == 1
                        """);
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));

        var requestResult = new HttpRequestResult(
                false, RequestMethod.GET, "uri", Map.of(), null,
                "body", "response",
                new MockVariables().put("x", "2"),
                200, Instant.now().toEpochMilli());
        when(requestService.executeRequest(anyString(), any(), anyBoolean()))
                .thenReturn(Optional.of(requestResult));

        var contextService = getContextService();
        var service = getService(contextService);
        service.execute("alias", false, false);

        System.out.println(service.getTestLog("alias"));
        assertTrue(service.getTestLog("alias").contains(TestServiceImpl.WARNING));
        assertFalse(service.getTestLog("alias").contains(TestServiceImpl.FAILED));
    }

    @Test
    void WHEN_strictTestNonExistingVariable_THEN_Failed() {
        var entity = new ApiTest().setAlias("alias").setGroup("group")
                .setPlan("""
                        x === 1
                        """);
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));

        var contextService = getContextService();
        var service = getService(contextService);
        service.execute("alias", false, false);

        System.out.println(service.getTestLog("alias"));
        assertFalse(service.getTestLog("alias").contains(TestServiceImpl.SUCCESS));
        assertFalse(service.getTestLog("alias").contains(TestServiceImpl.WARNING));
        assertTrue(service.getTestLog("alias").contains(TestServiceImpl.FAILED));
    }

    @Test
    void WHEN_saveVariableToContext_THEN_VariableExistsInContextWithCorrectValue() {
        var entity = new ApiTest().setAlias("alias").setGroup("group")
                .setPlan("""
                        request.id
                        x = ${x}
                        """);
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));

        var requestResult = new HttpRequestResult(
                false, RequestMethod.GET, "uri", Map.of(), null,
                "body", "response",
                new MockVariables().put("x", "2"),
                200, Instant.now().toEpochMilli());
        when(requestService.executeRequest(anyString(), any(), anyBoolean()))
                .thenReturn(Optional.of(requestResult));

        var contextService = getContextService();
        var service = getService(contextService);
        var result = service.execute("alias", false, false);

        System.out.println(service.getTestLog("alias"));
        assertEquals(TestRunStatus.OK, result);
        assertEquals("2", contextService.get().get("x"));
    }

    @Test
    void WHEN_testRequestByIdForCorrectResponseCodes_THEN_Success() {
        var entity = new ApiTest().setAlias("alias").setGroup("group")
                .setPlan("""
                        request.id -> 200
                        """);
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));

        var requestResult = new HttpRequestResult(
                false, RequestMethod.GET, "uri", Map.of(), null,
                "", "", new MockVariables(),
                200, Instant.now().toEpochMilli());
        when(requestService.executeRequest(anyString(), any(), anyBoolean()))
                .thenReturn(Optional.of(requestResult));

        var contextService = getContextService();
        var service = getService(contextService);
        var result = service.execute("alias", false, false);

        System.out.println(service.getTestLog("alias"));
        assertEquals(TestRunStatus.OK, result);
        assertTrue(service.getTestLog("alias").contains(TestServiceImpl.SUCCESS));
        assertFalse(service.getTestLog("alias").contains(TestServiceImpl.WARNING));
        assertFalse(service.getTestLog("alias").contains(TestServiceImpl.FAILED));
    }

    @Test
    void WHEN_testRequestByIdForIncorrectResponseCodes_THEN_Failed() {
        var entity = new ApiTest().setAlias("alias").setGroup("group")
                .setPlan("""
                        request.id -> 200
                        """);
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));

        var requestResult = new HttpRequestResult(
                false, RequestMethod.GET, "uri", Map.of(), null,
                "", "", new MockVariables(),
                404, Instant.now().toEpochMilli());
        when(requestService.executeRequest(anyString(), any(), anyBoolean()))
                .thenReturn(Optional.of(requestResult));

        var contextService = getContextService();
        var service = getService(contextService);
        var result = service.execute("alias", false, false);

        System.out.println(service.getTestLog("alias"));
        assertEquals(TestRunStatus.OK, result);
        assertFalse(service.getTestLog("alias").contains(TestServiceImpl.SUCCESS));
        assertFalse(service.getTestLog("alias").contains(TestServiceImpl.WARNING));
        assertTrue(service.getTestLog("alias").contains(TestServiceImpl.FAILED));
    }

    @Test
    void WHEN_testInlineRequestsForCorrectResponseCodes_THEN_Success() {
        var entity = new ApiTest().setAlias("alias").setGroup("group")
                .setPlan("""
                        GET localhost:8080 -> 200
                        POST localhost:8080 {} -> 200
                        
                        PUT localhost:8080 {}
                        
                        PATCH localhost:8080 {} -> 200
                        DELETE localhost:8080 {} -> 200
                        """);
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));

        var requestResult = new HttpRequestResult(
                false, RequestMethod.GET, "localhost:8080", Map.of(), null,
                "", "",
                new MockVariables(),
                200, Instant.now().toEpochMilli());
        when(httpService.request(any(), anyString(), anyString(), any()))
                .thenReturn(requestResult);

        var varService = getContextService();
        var service = getService(varService);
        var result = service.execute("alias", false, false);

        System.out.println(service.getTestLog("alias"));
        assertEquals(TestRunStatus.OK, result);
        assertTrue(service.getTestLog("alias").contains(TestServiceImpl.SUCCESS));
        assertFalse(service.getTestLog("alias").contains(TestServiceImpl.FAILED));
        assertFalse(service.getTestLog("alias").contains(TestServiceImpl.WARNING));
    }

    //
    //
    //
    // errors ------------------------------------
    //
    //
    //

    @Test
    void WHEN_requestThrows_THEN_Failed() {
        var entity = new ApiTest().setAlias("alias").setGroup("group")
                .setPlan("request.id");
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));
        when(requestService.executeRequest(anyString(), any(), anyBoolean()))
                .thenThrow(RuntimeException.class);

        var contextService = getContextService();
        var service = getService(contextService);
        var result = service.execute("alias", false, false);

        System.out.println(service.getTestLog("alias"));
        assertEquals(TestRunStatus.OK, result);
        assertTrue(service.getTestLog("alias").contains(TestServiceImpl.FAILED));
    }

    @Test
    void WHEN_inlineRequestThrows_THEN_Failed() {
        var entity = new ApiTest().setAlias("alias").setGroup("group")
                .setPlan("GET localhost:8080");
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));
        when(httpService.request(any(), any(), any(), any()))
                .thenThrow(RuntimeException.class);

        var contextService = getContextService();
        var service = getService(contextService);
        var result = service.execute("alias", false, false);

        System.out.println(service.getTestLog("alias"));
        assertEquals(TestRunStatus.OK, result);
        assertTrue(service.getTestLog("alias").contains(TestServiceImpl.FAILED));
    }

    @Test
    void WHEN_evaluateVarThrows_THEN_Failed() {
        var entity = new ApiTest().setAlias("alias").setGroup("group")
                .setPlan("x = 2");
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));
        doThrow(RuntimeException.class).when(contextService).put(any(), any());

        var service = getService(contextService);
        var result = service.execute("alias", false, false);

        System.out.println(service.getTestLog("alias"));
        assertEquals(TestRunStatus.OK, result);
        assertTrue(service.getTestLog("alias").contains(TestServiceImpl.FAILED));
    }

    @Test
    void WHEN_executeVarEqualsStrictThrows_THEN_Failed() {
        var entity = new ApiTest().setAlias("alias").setGroup("group")
                .setPlan("x === 2");
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));
        doThrow(RuntimeException.class).when(contextService).get();

        var service = getService(contextService);
        var result = service.execute("alias", false, false);

        System.out.println(service.getTestLog("alias"));
        assertEquals(TestRunStatus.OK, result);
        assertTrue(service.getTestLog("alias").contains(TestServiceImpl.FAILED));
    }

    @Test
    void WHEN_executeVarEqualsThrows_THEN_Failed() {
        var entity = new ApiTest().setAlias("alias").setGroup("group")
                .setPlan("x == 2");
        when(configRepository.findTest(anyString())).thenReturn(Optional.of(entity));
        doThrow(RuntimeException.class).when(contextService).get();

        var service = getService(contextService);
        var result = service.execute("alias", false, false);

        System.out.println(service.getTestLog("alias"));
        assertEquals(TestRunStatus.OK, result);
        assertTrue(service.getTestLog("alias").contains(TestServiceImpl.FAILED));
    }
}
