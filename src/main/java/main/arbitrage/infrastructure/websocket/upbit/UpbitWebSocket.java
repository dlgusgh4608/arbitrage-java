package main.arbitrage.infrastructure.websocket.upbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.infrastructure.websocket.WebSocketClient;
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
@RequiredArgsConstructor
@Slf4j
public class UpbitWebSocket implements WebSocketClient {
    private static final String UPBIT_WS_URL = "wss://api.upbit.com/websocket/v1";
    private static boolean isRunning = false;
    private WebSocketSession session;
    private final ObjectMapper objectMapper;

    private final String[] symbols = {"btc"};

    @Override
    public void connect() {
        if (isRunning) {
            throw new IllegalStateException("Upbit WebSocket is already running!");
        }

        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            WebSocketHandler handler = new UpbitWebSocketHandler(this::handleMessage);

            session = client.execute(handler, UPBIT_WS_URL).get();
            isRunning = true;

            sendSubscribeMessage();
            sendPing();

            log.info("Upbit WebSocket Connected time {}", LocalDateTime.now());
        } catch (InterruptedException | ExecutionException | IOException e) {
            log.error("Upbit WebSocket Connect Error {}", UPBIT_WS_URL, e);
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

    private void handlePongMessage(String status) {
        if (!"UP".equals(status)) {
            disconnect();
            return;
        }
        sendPing();
    }

    private void handleMessage(JsonNode message) {
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

//    private void handleMessage(String message) {
//        try {
//            log.info(message);
//
//            JsonNode node = objectMapper.readTree(message);
//            if (node.has("status")) {
//                handlePongMessage(node.get("status").asText());
//                return;
//            }
//            String type = node.get("type").asText();
//            String code = node.get("code").asText().replace("KRW-", "").toLowerCase();
//
//            switch (type) {
//                case "orderbook":
//                    log.info(node.toPrettyString());
//                    break;
//                case "trade":
//                    log.info(node.toPrettyString());
//                    break;
//                default:
//                    log.warn("Unknown message type: {}", type);
//            }
//        } catch (Exception e) {
//            log.error("Upbit WebSocket Error Processing Message: {}", message, e);
//        }
//    }

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