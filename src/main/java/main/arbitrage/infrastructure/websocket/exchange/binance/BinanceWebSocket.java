package main.arbitrage.infrastructure.websocket.exchange.binance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.infrastructure.websocket.common.BaseWebSocketClient;
import main.arbitrage.infrastructure.websocket.common.WebSocketClient;
import main.arbitrage.infrastructure.websocket.handler.MessageWebSocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class BinanceWebSocket extends BaseWebSocketClient implements WebSocketClient {
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
                log.info(data.get("s").asText());
            } else if (stream.endsWith("@depth10@100ms")) {
                log.info(data.get("s").asText());
            }
        } catch (Exception e) {
            log.error("Binance processing message {}", e.getMessage(), e);
        }

    }

    private String createStreamParams() {
        return Stream.concat(
                Arrays.stream(symbols).map(symbol -> symbol.toLowerCase() + "usdt" + "@aggTrade"),
                Arrays.stream(symbols).map(symbol -> symbol.toLowerCase() + "usdt" + "@depth10@100ms")
        ).collect(Collectors.joining("/"));
    }
}