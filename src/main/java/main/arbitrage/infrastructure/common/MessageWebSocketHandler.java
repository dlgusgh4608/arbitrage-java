package main.arbitrage.infrastructure.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageWebSocketHandler implements WebSocketHandler {
    private final ObjectMapper objectMapper;
    private final HashMap<String, Consumer<JsonNode>> sessionMap = new HashMap<>();

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        Consumer<JsonNode> messageHandler = sessionMap.get(session.getId());

        if (messageHandler == null) return;

        try {
            String payload;
            Object rawPayload = message.getPayload();

            if (rawPayload instanceof String) {
                payload = (String) rawPayload;
            } else if (rawPayload instanceof ByteBuffer) {
                ByteBuffer buffer = (ByteBuffer) rawPayload;
                payload = StandardCharsets.UTF_8.decode(buffer).toString();
            } else if (message instanceof TextMessage) {
                payload = ((TextMessage) message).getPayload();
            } else {
                payload = rawPayload.toString();
            }

            JsonNode jsonNode = objectMapper.readTree(payload);
            messageHandler.accept(jsonNode);
        } catch (Exception e) {
            log.error("{} WebSocket Error processing message: {}", session.getId(), message.getPayload(), e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("{} WebSocket connected", session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("{} WebSocket transport error", session.getId(), exception);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionMap.remove(session.getId());
        log.info("{} WebSocket connection closed: {}", session.getId(), status);
    }

    public void setMessageHandler(WebSocketSession session, Consumer<JsonNode> messageHandler) {
        sessionMap.put(session.getId(), messageHandler);
    }
}