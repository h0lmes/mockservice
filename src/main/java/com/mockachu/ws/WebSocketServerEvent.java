package com.mockachu.ws;

public record WebSocketServerEvent(WebSocketServerEventType event, String id, String data) {
}
