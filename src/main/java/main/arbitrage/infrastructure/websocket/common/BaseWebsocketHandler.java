package main.arbitrage.infrastructure.websocket.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Slf4j
public class BaseWebsocketHandler<T> extends AbstractWebSocketHandler {
  protected String socketName;
  protected final ObjectMapper objectMapper;
  protected final ConcurrentHashMap<String, WebSocketSession> sessionMap =
      new ConcurrentHashMap<>();
  protected Consumer<JsonNode> messageHandler;
  protected Runnable reconnectHandler = () -> {};

  public BaseWebsocketHandler(String socketName, ObjectMapper objectMapper) {
    this.socketName = socketName;
    this.objectMapper = objectMapper;
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    messageHandler.accept(objectMapper.readTree(message.getPayload()));
  }

  @Override
  protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message)
      throws Exception {
    String payload = StandardCharsets.UTF_8.decode(message.getPayload()).toString();
    messageHandler.accept(objectMapper.readTree(payload));
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    sessionMap.put(session.getId(), session);
    log.info("[{}]WebSocket connected\tid: {}", socketName, session.getId());
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    log.error("[{}]WebSocket transport error\tid: {}\n{}", socketName, session.getId(), exception);
  }

  @Override
  public boolean supportsPartialMessages() {
    return false;
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
      throws IOException {
    log.info(
        "[{}]WebSocket connection closed\tid: {}, status: {}", socketName, session.getId(), status);

    sessionMap.remove(session.getId());
    reconnectHandler.run();
  }

  public BaseWebsocketHandler<T> setMessageHandler(Consumer<JsonNode> messageHandler) {
    this.messageHandler = messageHandler;
    return this;
  }

  public BaseWebsocketHandler<T> setReconnectHandler(Runnable reconnectMethod) {
    this.reconnectHandler = reconnectMethod;
    return this;
  }

  public void sendMessage(T dto) {
    if (dto == null) return;

    TextMessage message = new TextMessage(objectMapper.valueToTree(dto).toString());

    for (Map.Entry<String, WebSocketSession> entry : sessionMap.entrySet()) {
      WebSocketSession session = entry.getValue();
      String sessionId = entry.getKey();

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
