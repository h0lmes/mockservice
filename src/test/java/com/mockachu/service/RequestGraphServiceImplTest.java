package com.mockachu.service;

import com.mockachu.domain.OutboundRequest;
import com.mockachu.domain.Route;
import com.mockachu.repository.ConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestGraphServiceImplTest {
    @Mock
    private ConfigRepository configRepository;

    private RequestGraphService service() {
        return new RequestGraphServiceImpl(configRepository);
    }

    @Test
    void WHEN_nothingTriggers_THEN_nothingFound() {
        var routeA = new Route()
                .setMethod(RequestMethod.POST)
                .setPath("/test/A")
                .setTriggerRequest(false);
        when(configRepository.findAllRoutes()).thenReturn(List.of(routeA));

        var reqA = new OutboundRequest()
                .setId("test.A")
                .setTriggerRequest(false);
        when(configRepository.findAllRequests()).thenReturn(List.of(reqA));

        var graph = service().getGraph();

        assertNotNull(graph);
        assertFalse(graph.contains(routeA.toString()));
        assertFalse(graph.contains(reqA.getId()));
    }

    @Test
    void WHEN_routeTriggersRequest_THEN_bothFound() {
        var routeA = new Route()
                .setMethod(RequestMethod.POST)
                .setPath("/test/A")
                .setTriggerRequest(true)
                .setTriggerRequestIds("test.A");
        when(configRepository.findAllRoutes()).thenReturn(List.of(routeA));

        var reqA = new OutboundRequest()
                .setId("test.A");
        when(configRepository.findAllRequests()).thenReturn(List.of(reqA));

        var graph = service().getGraph();

        assertNotNull(graph);
        assertFalse(graph.isBlank());
        assertTrue(graph.contains(routeA.toString()));
        assertTrue(graph.contains("100 ms")); // default value
        assertTrue(graph.contains(reqA.getId()));
    }

    @Test
    void WHEN_routeTriggersRequestWhichTriggersRequest_THEN_threeFound() {
        var routeA = new Route()
                .setMethod(RequestMethod.POST)
                .setPath("/test/path")
                .setTriggerRequest(true)
                .setTriggerRequestIds("test.A");
        when(configRepository.findAllRoutes()).thenReturn(List.of(routeA));

        var reqA = new OutboundRequest()
                .setId("test.A")
                .setTriggerRequest(true)
                .setTriggerRequestIds("test.B")
                .setTriggerRequestDelay("1000");
        var reqB = new OutboundRequest()
                .setId("test.B");
        when(configRepository.findAllRequests()).thenReturn(List.of(reqA, reqB));
        when(configRepository.findRequest(any()))
                .thenAnswer(args -> {
                    if ("test.A".equals(args.getArgument(0))) return Optional.of(reqA);
                    if ("test.B".equals(args.getArgument(0))) return Optional.of(reqB);
                    return Optional.empty();
                });

        var graph = service().getGraph();

        assertNotNull(graph);
        assertFalse(graph.isBlank());
        assertTrue(graph.contains(routeA.toString()));
        assertTrue(graph.contains(reqA.getId()));
        assertTrue(graph.contains(reqB.getId()));
    }

    @Test
    void WHEN_requestTriggersAnotherRequest_THEN_bothFound() {
        var reqA = new OutboundRequest()
                .setId("test.A")
                .setTriggerRequest(true)
                .setTriggerRequestIds("test.B")
                .setTriggerRequestDelay("1000");
        var reqB = new OutboundRequest()
                .setId("test.B");
        when(configRepository.findAllRequests()).thenReturn(List.of(reqA, reqB));

        var graph = service().getGraph();

        assertNotNull(graph);
        assertFalse(graph.isBlank());
        assertTrue(graph.contains(reqA.getId()));
        assertTrue(graph.contains("1000 ms"));
        assertTrue(graph.contains(reqB.getId()));
    }

    @Test
    void WHEN_requestTriggersItself_THEN_cycleFound() {
        var reqA = new OutboundRequest()
                .setId("test.A")
                .setTriggerRequest(true)
                .setTriggerRequestIds("test.A");
        when(configRepository.findAllRequests()).thenReturn(List.of(reqA));

        var graph = service().getGraph();

        assertNotNull(graph);
        assertFalse(graph.isBlank());
        assertTrue(graph.contains("CYCLE"));
    }
}
