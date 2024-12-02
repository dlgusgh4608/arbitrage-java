package main.arbitrage.infrastructure.websocket.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Slf4j
public class ExchangeWebSocketHandler extends AbstractWebSocketHandler {
    private Consumer<JsonNode> messageHandler;
    private String socketName;
    private final ObjectMapper objectMapper;

    public ExchangeWebSocketHandler(
            String socketName,
            Consumer<JsonNode> messageHandler,
            ObjectMapper objectMapper
    ) {
        this.socketName = socketName;
        this.messageHandler = messageHandler;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        messageHandler.accept(objectMapper.readTree(message.getPayload()));
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String payload = StandardCharsets.UTF_8.decode(message.getPayload()).toString();
        messageHandler.accept(objectMapper.readTree(payload));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("{}:{} WebSocket connected", session.getId(), socketName);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("{}:{} WebSocket transport error", session.getId(), socketName, exception);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("{}:{} WebSocket connection closed: {}", session.getId(), socketName, status);
    }
}