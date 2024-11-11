package main.arbitrage.domain.symbolPrice.infrastructure.websocket.exchange.upbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import main.arbitrage.domain.symbolPrice.infrastructure.websocket.common.BaseWebSocketClient;
import main.arbitrage.domain.symbolPrice.dto.TradeDto;
import main.arbitrage.domain.symbolPrice.dto.OrderbookDto;
import main.arbitrage.domain.symbolPrice.infrastructure.websocket.handler.MessageWebSocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.List;


@Component
@Slf4j
public class UpbitWebSocket extends BaseWebSocketClient {
    private static final String WS_URL = "wss://api.upbit.com/websocket/v1";
    private static boolean isRunning = false;
    private WebSocketSession session;

    private final String[] symbols = {"btc"};

    public UpbitWebSocket(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void connect() {
        if (isRunning) {
            throw new IllegalStateException("Upbit WebSocket is already running!");
        }

        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            WebSocketHandler handler = new MessageWebSocketHandler("Upbit", this::handleMessage);

            session = client.execute(handler, WS_URL).get();
            isRunning = true;

            sendSubscribeMessage();
            sendPing();
        } catch (InterruptedException | ExecutionException | IOException e) {
            log.error("Upbit WebSocket Connect Error {}", WS_URL, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disconnect() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
                isRunning = false;
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

            switch (type) {
                case "orderbook":
                    handleOrderbook(message);
                    break;
                case "trade":
                    handleTrade(message);
                    break;
                default:
                    log.warn("Unknown message type: {}", type);
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }

    @Override
    protected void handleTrade(JsonNode data) {
        String symbol = data.get("code").asText().replace("KRW-", "").toLowerCase();

        TradeDto trade = TradeDto.builder()
                .symbol(symbol)
                .price(new BigDecimal(data.get("trade_price").asText()))
                .timestamp(data.get("trade_timestamp").asLong())
                .build();

        tradeMap.put(symbol, trade);
    }

    @Override
    protected void handleOrderbook(JsonNode data) {
        String symbol = data.get("code").asText().replace("KRW-", "").toLowerCase();

        OrderbookDto orderbook = OrderbookDto.builder()
                .symbol(symbol)
                .bids(createOrderbookUnits(data.get("orderbook_units"), false))
                .asks(createOrderbookUnits(data.get("orderbook_units"), true))
                .build();

        orderbookMap.put(symbol, orderbook);
    }

    private OrderbookDto.OrderbookUnit[] createOrderbookUnits(JsonNode units, boolean isAsk) {
        OrderbookDto.OrderbookUnit[] orderbooksUnits = new OrderbookDto.OrderbookUnit[10];
        String type;
        type = isAsk ? "ask" : "bid";

        for (int i = 0; i < 10; i++) {
            JsonNode unit = units.get(i);
            orderbooksUnits[i] = OrderbookDto.OrderbookUnit.builder()
                    .price(new BigDecimal(unit.get(type.concat("_price")).asText()))
                    .size(new BigDecimal(unit.get(type.concat("_size")).asText()))
                    .build();
        }

        return orderbooksUnits;
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
        session.sendMessage(new TextMessage(message));
    }
}