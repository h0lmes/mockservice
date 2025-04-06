package com.mockachu.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper jsonMapper;

    public int getNumberOfClients() {
        return sessions.size();
    }

    public WebSocketHandler(@Qualifier("jsonMapper") ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void broadcastTestResult(String testAlias, String testResult) {
        broadcastMessage(
                new WebSocketServerEvent(WebSocketServerEventType.TEST_RESULT, testAlias, testResult));
    }

    public void broadcastRouteRequest(String method, String path, String alt) {
        String routeId = method + ";" + path + ";" + alt;
        broadcastMessage(
                new WebSocketServerEvent(WebSocketServerEventType.ROUTE_REQUEST, routeId, ""));
    }

    private void broadcastMessage(WebSocketServerEvent payload) {
        removeClosedSessions();
        if (sessions.isEmpty()) return;

        try {
            var serialized = jsonMapper.writeValueAsString(payload);
            var message = new TextMessage(serialized);
            for (WebSocketSession s : sessions) {
                s.sendMessage(message);
            }
        } catch (IOException e) {
            log.error("Error sending WS message: ", e);
        }
    }

    private void removeClosedSessions() {
        sessions.removeIf(s -> !s.isOpen());
    }

    @Override
    public void handleTextMessage(@Nonnull WebSocketSession session, @Nonnull TextMessage message)
            throws InterruptedException, IOException {
        log.info("WS message: {}", message.getPayload());
    }

    @Override
    public void afterConnectionEstablished(@Nonnull WebSocketSession session) {
        InetSocketAddress clientAddress = session.getRemoteAddress();
        if (clientAddress != null) {
            log.info("Accepted WS connection from: {}:{}", clientAddress.getHostString(), clientAddress.getPort());
        }
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(@Nonnull WebSocketSession session,
                                      @Nonnull CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("WS connection closed");
        super.afterConnectionClosed(session, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WS transport error: ", exception);
    }
}
