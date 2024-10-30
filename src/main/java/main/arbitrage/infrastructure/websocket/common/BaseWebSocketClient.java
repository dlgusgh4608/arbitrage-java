package main.arbitrage.infrastructure.websocket.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseWebSocketClient {
    protected ObjectMapper objectMapper;

    public BaseWebSocketClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected abstract void handleMessage(JsonNode message);
}