package main.arbitrage.infrastructure.websocket.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseWebSocketClient {
    protected static String wsUrl;
    protected static boolean isRunning = false;
    protected ObjectMapper objectMapper;

    public BaseWebSocketClient(String url, ObjectMapper objectMapper) {
        wsUrl = url;
        this.objectMapper = objectMapper;
    }

    protected abstract void handleMessage(JsonNode message);
}