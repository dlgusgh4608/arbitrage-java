package main.arbitrage.infrastructure.websocket.exchange.upbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import main.arbitrage.infrastructure.websocket.common.BaseWebSocketClient;
import main.arbitrage.infrastructure.websocket.common.WebSocketClient;
import main.arbitrage.infrastructure.websocket.handler.MessageWebSocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.List;


@Component
@Slf4j
public class UpbitWebSocket extends BaseWebSocketClient implements WebSocketClient {
    private WebSocketSession session;

    private final String[] symbols = {"btc"};

    public UpbitWebSocket(ObjectMapper objectMapper) {
        super("wss://api.upbit.com/websocket/v1", objectMapper);
    }

    @Override
    public void connect() {
        if (isRunning) {
            throw new IllegalStateException("Upbit WebSocket is already running!");
        }

        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            WebSocketHandler handler = new MessageWebSocketHandler("Upbit", this::handleMessage);

            session = client.execute(handler, wsUrl).get();
            isRunning = true;

            sendSubscribeMessage();
            sendPing();

            log.info("Upbit WebSocket Connected time {}", LocalDateTime.now());
        } catch (InterruptedException | ExecutionException | IOException e) {
            log.error("Upbit WebSocket Connect Error {}", wsUrl, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disconnect() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
                isRunning = false;
                log.info("Upbit WebSocket Disconnected");
            } catch (IOException e) {
                log.error("Error closing Upbit WebSocket", e);
            }
        }
    }

    @Override
    public boolean isConnected() {
        return session != null && session.isOpen() && isRunning;
    }

    @Override
    protected void handleMessage(JsonNode message) {
        try {
            if (message.has("status")) {
                handlePongMessage(message.get("status").asText());
                return;
            }

            String type = message.get("type").asText();
            String code = message.get("code").asText().replace("KRW-", "").toLowerCase();

            switch (type) {
                case "orderbook":
                    log.info(message.toPrettyString());
                    break;
                case "trade":
                    log.info(message.toPrettyString());
                    break;
                default:
                    log.warn("Unknown message type: {}", type);
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }

    private void handlePongMessage(String status) {
        if (!"UP".equals(status)) {
            disconnect();
            return;
        }
        sendPing();
    }

    private void sendPing() {
        try {
            session.sendMessage(new TextMessage("PING"));
        } catch (IOException e) {
            log.error("Failed to send ping message", e);
        }
    }

    private void sendSubscribeMessage() throws IOException {
        List<UpbitSubscribeMessage> messages = UpbitSubscribeMessage.createSubscribeMessage("unique_ticket_123", symbols);
        String message = objectMapper.writeValueAsString(messages);
        log.info(message);
        session.sendMessage(new TextMessage(message));
    }
}