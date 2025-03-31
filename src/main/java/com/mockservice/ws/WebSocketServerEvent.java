package com.mockservice.ws;

public record WebSocketServerEvent(WebSocketServerEventType event, String id, String data) {
}
