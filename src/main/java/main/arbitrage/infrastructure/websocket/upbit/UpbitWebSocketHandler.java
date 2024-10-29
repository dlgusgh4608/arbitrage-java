package main.arbitrage.infrastructure.websocket.upbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Slf4j
public class UpbitWebSocketHandler implements WebSocketHandler {
    private final Consumer<JsonNode> messageHandler;
    private final ObjectMapper objectMapper;

    public UpbitWebSocketHandler(Consumer<JsonNode> messageHandler) {
        this.messageHandler = messageHandler;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        try {
            String payload;
            Object rawPayload = message.getPayload();

            ByteBuffer buffer = (ByteBuffer) rawPayload;
            payload = StandardCharsets.UTF_8.decode(buffer).toString();
            
            JsonNode jsonNode = objectMapper.readTree(payload);
            log.info(jsonNode.toPrettyString());
            messageHandler.accept(jsonNode);
        } catch (Exception e) {
            log.error("Error processing message: {}", message.getPayload(), e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Upbit WebSocket connected");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Upbit WebSocket transport error", exception);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Upbit WebSocket connection closed: {}", status);
    }
}