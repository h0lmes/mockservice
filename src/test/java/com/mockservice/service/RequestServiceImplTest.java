package com.mockservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.config.WebClientFactory;
import com.mockservice.domain.OutboundRequest;
import com.mockservice.mapper.OutboundRequestMapperImpl;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.template.TemplateEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private ConfigRepository configRepository;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private VariablesService variablesService;

    private RequestService getService() {
        return new RequestServiceImpl(
                new WebClientFactory("", ""),
                configRepository, new OutboundRequestMapperImpl(), templateEngine,
                variablesService, 256, new ObjectMapper());
    }

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
    void executeRequest() {
        var entity = new OutboundRequest().setId("test_id").setPath("127.0.0.1:65530");
        when(configRepository.findRequest(anyString())).thenReturn(Optional.of(entity));
        var service = getService();
        var result = service.executeRequest("id", null, false);

        assertTrue(result.isPresent());
        assertTrue(result.get().isFailed());
        System.out.println(result.get().getResponseBody());
        assertTrue(result.get().getResponseBody().contains("127.0.0.1:65530"));
    }
}
