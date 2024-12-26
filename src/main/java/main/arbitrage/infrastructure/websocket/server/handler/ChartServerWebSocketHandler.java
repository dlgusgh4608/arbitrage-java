package main.arbitrage.infrastructure.websocket.server.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.application.collector.dto.ChartBySymbolDTO;
import main.arbitrage.infrastructure.websocket.server.BaseServerSocketHandler;

@Component
@Slf4j
public class ChartServerWebSocketHandler extends BaseServerSocketHandler<ChartBySymbolDTO> {
    public ChartServerWebSocketHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    private String extractSymbol(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1).toUpperCase();
    }

    @Override
    public void sendMessage(ChartBySymbolDTO data) {
        if (data == null)
            return;

        TextMessage message = new TextMessage(objectMapper.valueToTree(data).toString());
        List<String> sessionsToRemove = new ArrayList<>();

        for (Map.Entry<String, WebSocketSession> entry : sessionMap.entrySet()) {
            WebSocketSession session = entry.getValue();
            String sessionId = entry.getKey();

            if (session == null || !session.isOpen()) {
                sessionsToRemove.add(sessionId);
                continue;
            }

            if (!extractSymbol(session).equals(data.getSymbol())) {
                continue;
            }

            synchronized (session) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    log.error("Failed to send message to session {}: {}", sessionId,
                            e.getMessage());
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
