package com.mockservice.service;

import com.mockservice.response.MockResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestServiceImplTest {

    @Mock
    private MockResponse response;

    @DisplayName("TODO: Find a better way to test async action with WebClient in it")
    @Test
    public void schedule() {
        String url = "http://localhost:8087";
        when(response.getRequestUrl()).thenReturn(url);
        when(response.getRequestMethod()).thenReturn(HttpMethod.GET);
        when(response.getRequestBody()).thenReturn("");
        when(response.getRequestHeaders()).thenReturn(new HttpHeaders());

        RequestService service = new RequestServiceImpl();
        service.schedule(response);
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(response, atLeastOnce()).getRequestUrl();
        verify(response, atLeastOnce()).getRequestMethod();
        verify(response, atLeastOnce()).getRequestBody();
        verify(response, atLeastOnce()).getRequestHeaders();
    }
}
