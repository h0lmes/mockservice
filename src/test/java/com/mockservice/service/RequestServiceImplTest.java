package com.mockservice.service;

import com.mockservice.mapper.OutboundRequestMapper;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.response.MockResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RequestServiceImplTest {

    @Mock
    private MockResponse response;
    @Mock
    private ConfigRepository configRepository;
    @Mock
    private OutboundRequestMapper requestMapper;

    @DisplayName("TODO: Find a better way to test async action with WebClient in it")
    @Test
    public void schedule() {
//        String url = "http://localhost:8087";
//        when(response.getRequestUrl()).thenReturn(url);
//        when(response.getRequestMethod()).thenReturn(HttpMethod.GET);
//        when(response.getRequestBody()).thenReturn("");
//        when(response.getRequestHeaders()).thenReturn(new HttpHeaders());
//
//        RequestService service = new RequestServiceImpl(configRepository, requestMapper);
//        service.schedule(response);
//        try {
//            Thread.sleep(2500);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        verify(response, atLeastOnce()).getRequestUrl();
//        verify(response, atLeastOnce()).getRequestMethod();
//        verify(response, atLeastOnce()).getRequestBody();
//        verify(response, atLeastOnce()).getRequestHeaders();
    }
}
