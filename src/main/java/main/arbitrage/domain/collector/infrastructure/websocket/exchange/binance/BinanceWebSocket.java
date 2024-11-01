package main.arbitrage.domain.collector.infrastructure.websocket.exchange.binance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.domain.collector.infrastructure.websocket.common.BaseWebSocketClient;
import main.arbitrage.domain.collector.dto.TradeDto;
import main.arbitrage.domain.collector.dto.OrderbookDto;
import main.arbitrage.domain.collector.infrastructure.websocket.handler.MessageWebSocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class BinanceWebSocket extends BaseWebSocketClient {
    private static String WS_URL = "wss://fstream.binance.com/stream?streams=";
    private static boolean isRunning = false;
    private WebSocketSession session;

    private final String[] symbols = {"btc"};

    public BinanceWebSocket(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void connect() {
        if (isRunning) {
            throw new IllegalStateException("Binance WebSocket is already running!");
        }

        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            WebSocketHandler handler = new MessageWebSocketHandler("Binance", this::handleMessage);
            String params = createStreamParams();

            WS_URL = WS_URL.concat(params);

            session = client.execute(handler, WS_URL).get();
            isRunning = true;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Binance WebSocket Connect Error {}", WS_URL, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disconnect() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
                isRunning = false;
                log.info("Binance WebSocket Disconnected");
            } catch (IOException e) {
                log.error("Error closing Binance WebSocket", e);
            }
        }
    }

    @Override
    public boolean isConnected() {
        return session != null && session.isOpen() && isRunning;
    }

    @Override
    protected void handleMessage(JsonNode message) {
        String stream = message.get("stream").asText();
        JsonNode data = message.get("data");

        try {
            if (stream.endsWith("@aggTrade")) {
                handleTrade(data);
            } else if (stream.endsWith("@depth10@100ms")) {
                handleOrderbook(data);
            }
        } catch (Exception e) {
            log.error("Binance processing message {}", e.getMessage(), e);
        }

    }

    @Override
    protected void handleTrade(JsonNode data) {
        String symbol = data.get("s").asText().toLowerCase().replace("usdt", "");

        TradeDto trade = TradeDto.builder()
                .symbol(symbol)
                .price(new BigDecimal(data.get("p").asText()))
                .timestamp(data.get("T").asLong())
                .build();

        tradeMap.put(symbol, trade);
    }

    @Override
    protected void handleOrderbook(JsonNode data) {
        String symbol = data.get("s").asText().toLowerCase().replace("usdt", "");

        OrderbookDto orderbook = OrderbookDto.builder()
                .symbol(symbol)
                .bids(createOrderbookUnits(data.get("b")))
                .asks(createOrderbookUnits(data.get("a")))
                .build();

        orderbookMap.put(symbol, orderbook);
    }

    private OrderbookDto.OrderbookUnit[] createOrderbookUnits(JsonNode units) {
        OrderbookDto.OrderbookUnit[] orderbooksUnits = new OrderbookDto.OrderbookUnit[10];

        for (int i = 0; i < 10; i++) {
            JsonNode unit = units.get(i);
            orderbooksUnits[i] = OrderbookDto.OrderbookUnit.builder()
                    .price(new BigDecimal(unit.get(0).asText()))
                    .size(new BigDecimal(unit.get(1).asText()))
                    .build();
        }
        return orderbooksUnits;
    }

    private String createStreamParams() {
        return Stream.concat(
                Arrays.stream(symbols).map(symbol -> symbol.toLowerCase() + "usdt" + "@aggTrade"),
                Arrays.stream(symbols).map(symbol -> symbol.toLowerCase() + "usdt" + "@depth10@100ms")
        ).collect(Collectors.joining("/"));
    }
}