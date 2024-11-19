package main.arbitrage.infrastructure.common.websocket.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientWebSocketHandler implements WebSocketHandler {
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionMap.put(session.getId(), session);
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
        log.info("{} WebSocket connection closed: {}", session.getId(), status);
    }

    public void sendMessage(JsonNode json) {
        if (json == null) {
            log.warn("Attempted to send null message");
            return;
        }

        TextMessage message = new TextMessage(json.toString());
        List<String> sessionsToRemove = new ArrayList<>();

        for (Map.Entry<String, WebSocketSession> entry : sessionMap.entrySet()) {
            WebSocketSession session = entry.getValue();
            String sessionId = entry.getKey();

            if (session == null || !session.isOpen()) {
                sessionsToRemove.add(sessionId);
                continue;
            }

            synchronized (session) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    log.error("Failed to send message to session {}: {}", sessionId, e.getMessage());
                    sessionsToRemove.add(sessionId);
                    try {
                        session.close();
                    } catch (IOException ex) {
                        log.error("Error closing session {}: {}", sessionId, ex.getMessage());
                    }
                }
            }
        }
        
        sessionsToRemove.forEach(sessionMap::remove);
    }
}