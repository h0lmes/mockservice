package com.mockachu.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebSocketHandlerTest {

    private ObjectMapper jsonMapper = new ObjectMapper();
    private String sentMessage;

    @Test
    void testAddRemoveSession() throws Exception {
        var handler = new WebSocketHandler(jsonMapper);
        var session = getSession("1", true);

        assertEquals(0, handler.getNumberOfClients());
        handler.afterConnectionEstablished(session);
        assertEquals(1, handler.getNumberOfClients());
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
        assertEquals(0, handler.getNumberOfClients());
    }

    @Test
    void testBroadcastTestResult() {
        var handler = new WebSocketHandler(jsonMapper);
        var session = getSession("2", true);

        handler.afterConnectionEstablished(session);
        handler.broadcastTestResult("test_alias", "test_result");
        assertTrue(sentMessage.contains("test_alias"));
        assertTrue(sentMessage.contains("test_result"));
    }

    @Test
    void testBroadcastWithNoSessions() {
        sentMessage = null;
        var handler = new WebSocketHandler(jsonMapper);

        handler.broadcastTestResult("test_alias", "test_result");
        assertNull(sentMessage);
    }

    @Test
    void testBroadcastWithClosedSession() {
        sentMessage = null;
        var handler = new WebSocketHandler(jsonMapper);
        var session = getSession("3", false);

        handler.afterConnectionEstablished(session);
        handler.broadcastTestResult("test_alias", "test_result");
        assertNull(sentMessage);
    }

    private WebSocketSession getSession(String id, boolean open) {
        return new WebSocketSession() {

            @Override
            public String getId() {
                return id;
            }

            @Override
            public URI getUri() {
                return URI.create("localhost");
            }

            @Override
            public HttpHeaders getHandshakeHeaders() {
                return null;
            }

            @Override
            public Map<String, Object> getAttributes() {
                return null;
            }

            @Override
            public Principal getPrincipal() {
                return null;
            }

            @Override
            public InetSocketAddress getLocalAddress() {
                return new InetSocketAddress("localhost", 8080);
            }

            @Override
            public InetSocketAddress getRemoteAddress() {
                return new InetSocketAddress("localhost", 28080);
            }

            @Override
            public String getAcceptedProtocol() {
                return null;
            }

            @Override
            public void setTextMessageSizeLimit(int messageSizeLimit) {

            }

            @Override
            public int getTextMessageSizeLimit() {
                return 0;
            }

            @Override
            public void setBinaryMessageSizeLimit(int messageSizeLimit) {

            }

            @Override
            public int getBinaryMessageSizeLimit() {
                return 0;
            }

            @Override
            public List<WebSocketExtension> getExtensions() {
                return null;
            }

            @Override
            public void sendMessage(WebSocketMessage<?> message) throws IOException {
                if (message instanceof TextMessage textMessage) {
                    sentMessage = textMessage.getPayload();
                }
            }

            @Override
            public boolean isOpen() {
                return open;
            }

            @Override
            public void close() throws IOException {

            }

            @Override
            public void close(CloseStatus status) throws IOException {

            }
        };
    }
}
