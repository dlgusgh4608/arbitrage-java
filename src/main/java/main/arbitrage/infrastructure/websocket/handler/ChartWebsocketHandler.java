package main.arbitrage.infrastructure.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.application.collector.dto.ChartBySymbolDTO;
import main.arbitrage.infrastructure.websocket.common.BaseWebsocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
@Slf4j
public class ChartWebsocketHandler extends BaseWebsocketHandler<ChartBySymbolDTO> {
  public ChartWebsocketHandler(ObjectMapper objectMapper) {
    super("Chart to Client", objectMapper);
  }

  private String extractSymbol(WebSocketSession session) {
    String path = session.getUri().getPath();
    return path.substring(path.lastIndexOf('/') + 1).toUpperCase();
  }

  @Override
  public void sendMessage(ChartBySymbolDTO data) {
    if (data == null) return;

    TextMessage message = new TextMessage(objectMapper.valueToTree(data).toString());

    for (Map.Entry<String, WebSocketSession> entry : sessionMap.entrySet()) {
      WebSocketSession session = entry.getValue();
      String sessionId = entry.getKey();

      if (!extractSymbol(session).equals(data.getSymbol())) continue;

      synchronized (session) {
        try {
          session.sendMessage(message);
        } catch (IOException e) {
          log.error(
              "[{}]Failed to send message to session\tid: {}\n{}",
              socketName,
              sessionId,
              e.getMessage());
          try {
            session.close();
          } catch (IOException ex) {
            log.error("[{}]Error closing session\tid: {}\n{}", socketName, sessionId, ex);
          }
        }
      }
    }
  }
}
