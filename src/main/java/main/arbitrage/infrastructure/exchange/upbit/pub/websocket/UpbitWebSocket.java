package main.arbitrage.infrastructure.exchange.upbit.pub.websocket;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.infrastructure.exchange.dto.OrderbookDto;
import main.arbitrage.infrastructure.exchange.dto.TradeDto;
import main.arbitrage.infrastructure.websocket.client.BaseWebSocketClient;
import main.arbitrage.infrastructure.websocket.client.handler.ExchangeWebSocketHandler;


@Component
@Slf4j
@RequiredArgsConstructor
public class UpbitWebSocket extends BaseWebSocketClient {
    private final SymbolVariableService symbolVariableService;
    private static final String WS_URL = "wss://api.upbit.com/websocket/v1";
    private List<String> symbols;
    private static boolean isRunning = false;
    private final ObjectMapper objectMapper;

    private WebSocketSession session;

    @PostConstruct
    private void init() {
        symbols =
                symbolVariableService.getSupportedSymbols().stream().map(Symbol::getName).toList();
    }

    @Override
    public void connect() {
        if (isRunning) {
            throw new IllegalStateException("Upbit WebSocket is already running!");
        }

        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            ExchangeWebSocketHandler handler =
                    new ExchangeWebSocketHandler("Upbit", this::handleMessage, objectMapper);
            session = client.execute(handler, WS_URL).get();
            isRunning = true;

            sendSubscribeMessage(symbols);
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

        TradeDto trade = TradeDto.builder().symbol(symbol)
                .price(Double.parseDouble(data.get("trade_price").asText()))
                .timestamp(data.get("trade_timestamp").asLong()).build();

        tradeMap.put(symbol, trade);
    }

    @Override
    protected void handleOrderbook(JsonNode data) {
        String symbol = data.get("code").asText().replace("KRW-", "").toLowerCase();

        OrderbookDto orderbook = OrderbookDto.builder().symbol(symbol)
                .bids(createOrderbookUnits(data.get("orderbook_units"), false))
                .asks(createOrderbookUnits(data.get("orderbook_units"), true)).build();

        orderbookMap.put(symbol, orderbook);
    }

    private OrderbookDto.OrderbookUnit[] createOrderbookUnits(JsonNode units, boolean isAsk) {
        OrderbookDto.OrderbookUnit[] orderbooksUnits = new OrderbookDto.OrderbookUnit[10];
        String type;
        type = isAsk ? "ask" : "bid";

        for (int i = 0; i < 10; i++) {
            JsonNode unit = units.get(i);
            orderbooksUnits[i] = OrderbookDto.OrderbookUnit.builder()
                    .price(Double.parseDouble(unit.get(type.concat("_price")).asText()))
                    .size(Double.parseDouble(unit.get(type.concat("_size")).asText())).build();
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

    private void sendSubscribeMessage(List<String> symbols) throws IOException {
        List<UpbitSubscribeMessage> messages =
                UpbitSubscribeMessage.createSubscribeMessage("unique_ticket_123", symbols);
        String message = objectMapper.writeValueAsString(messages);
        session.sendMessage(new TextMessage(message));
    }
}
