package com.mockachu.service;

import com.mockachu.domain.OutboundRequest;
import com.mockachu.mapper.OutboundRequestMapperImpl;
import com.mockachu.model.HttpRequestResult;
import com.mockachu.model.OutboundRequestDto;
import com.mockachu.repository.ConfigRepository;
import com.mockachu.template.MockVariables;
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
class RequestServiceImplTest {

    @Mock
    private ConfigRepository configRepository;
    @Mock
    private ContextService contextService;
    @Mock
    private HttpService httpService;

    private RequestService getService() {
        return new RequestServiceImpl(256, configRepository,
                new OutboundRequestMapperImpl(), contextService, httpService);
    }

    //---------------------------------------------------------------------
    //
    // repo operations
    //
    //---------------------------------------------------------------------

    @Test
    void getRequests() {
        var entity = new OutboundRequest()
                .setId("test_id")
                .setPath("localhost:65530")
                .setMethod(RequestMethod.GET)
                .setGroup("test_group")
                .setBody("body")
                .setHeaders("Test: test");
        when(configRepository.findAllRequests()).thenReturn(List.of(entity));
        var service = getService();
        var list = service.getRequests();

        assertEquals(1, list.size());
        assertEquals("test_id", list.get(0).getId());
        assertEquals("localhost:65530", list.get(0).getPath());
        assertEquals(RequestMethod.GET, list.get(0).getMethod());
        assertEquals("test_group", list.get(0).getGroup());
        assertEquals("body", list.get(0).getBody());
        assertEquals("Test: test", list.get(0).getHeaders());
    }

    @Test
    void putTest_DtoAsInput_CallsRepositoryMethod() throws IOException {
        var service = getService();
        service.putRequest(new OutboundRequestDto(), new OutboundRequestDto());

        verify(configRepository, times(1)).putRequest(any(), any());
    }

    @Test
    void putTests_ListOfDtoAsInput_CallsRepositoryMethod() throws IOException {
        var service = getService();
        service.putRequests(List.of(new OutboundRequestDto()), true);

        verify(configRepository, times(1)).putRequests(anyList(), anyBoolean());
    }

    @SuppressWarnings("unchecked")
    @Test
    void deleteTests_ListOfDtoAsInput_CallsRepositoryMethod() throws IOException {
        var service = getService();
        service.deleteRequests(List.of(new OutboundRequestDto().setId("id")));

        ArgumentCaptor<List<OutboundRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(configRepository).deleteRequests(captor.capture());
        assertFalse(captor.getValue().isEmpty());
        assertEquals("id", captor.getValue().get(0).getId());
    }

    //---------------------------------------------------------------------
    //
    // execution
    //
    //---------------------------------------------------------------------

    @Test
    void executeRequest() {
        var res = Optional.of(new HttpRequestResult(
                false, RequestMethod.GET, "uri", Map.of(),
                "{\"test\": \"test\"}", "reqBody",
                MockVariables.empty(), 200, Instant.now().toEpochMilli()));
        when(httpService.request(any(), anyString(), anyString(), any())).thenReturn(res);

        var entity = new OutboundRequest().setId("test_id");
        when(configRepository.findRequest(anyString())).thenReturn(Optional.of(entity));
        var service = getService();
        var result = service.executeRequest("id", null, false);

        assertTrue(result.isPresent());
        assertFalse(result.get().isFailed());
    }
}
