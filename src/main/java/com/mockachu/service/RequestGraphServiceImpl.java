package com.mockachu.service;

import com.mockachu.domain.OutboundRequest;
import com.mockachu.domain.Route;
import com.mockachu.repository.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class RequestGraphServiceImpl implements RequestGraphService {
    private static final Logger log = LoggerFactory.getLogger(RequestGraphServiceImpl.class);

    private final ConfigRepository repository;

    public RequestGraphServiceImpl(ConfigRepository repository) {
        this.repository = repository;
    }

    public String getGraph() {
        StringBuilder builder = new StringBuilder();
        var routes = repository.findAllRoutes();
        var requests = repository.findAllRequests();

        for (Route route : routes) {
            if (route.getDisabled() ||
                    !route.isTriggerRequest() ||
                    route.getTriggerRequestIds().isBlank()) continue;

            String base = route.toString();
            Set<String> seen = new HashSet<>();
            seen.add(base);
            dfs(builder, base, route.getTriggerRequestIds(), route.getTriggerRequestDelay(), seen);
        }

        for (OutboundRequest request : requests) {
            if (!request.isTriggerRequest() ||
                    request.getTriggerRequestIds().isBlank()) continue;

            String base = request.getId();
            Set<String> seen = new HashSet<>();
            seen.add(base);
            dfs(builder, base, request.getTriggerRequestIds(), request.getTriggerRequestDelay(), seen);
        }

        return builder.toString();
    }

    private void dfs(StringBuilder builder,
                     String base,
                     String requestIds,
                     String requestDelays,
                     Set<String> seen
    ) {
        if (requestIds.isBlank()) {
            if (!builder.isEmpty()) builder.append("\n\n");
            builder.append(base);
            return;
        }

        var idArray = requestIds.split(",");
        var delayArray = requestDelays.split(",");

        for (int i = 0; i < idArray.length; i++) {
            String requestId = idArray[i].trim();
            String delay = "100";
            if (i < delayArray.length && !delayArray[i].isBlank()) delay = delayArray[i].trim();
            String newBase = i == 0 ? base : " ".repeat(base.length());
            newBase += " -> " + delay + " ms -> " + requestId;

            if (seen.contains(requestId)) {
                newBase += " CYCLE";
                dfs(builder, newBase, "", "", seen);
                continue;
            }

            var nextRequest = repository.findRequest(requestId).orElse(null);
            if (nextRequest == null || !nextRequest.isTriggerRequest()) {
                dfs(builder, newBase, "", "", seen);
                continue;
            }

            Set<String> newSeen = new HashSet<>(seen);
            newSeen.add(requestId);
            dfs(builder, newBase,
                    nextRequest.getTriggerRequestIds(),
                    nextRequest.getTriggerRequestDelay(), newSeen);
        }
    }
}
