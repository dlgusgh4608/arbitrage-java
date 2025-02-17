package main.arbitrage.infrastructure.binance.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.infrastructure.exchanges.dto.OrderbookDto;
import main.arbitrage.infrastructure.exchanges.dto.TradeDto;
import main.arbitrage.infrastructure.websocket.client.ExchangeWebsocketClient;
import main.arbitrage.infrastructure.websocket.handler.BinancePublicWebsocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class BinanceWebSocket extends ExchangeWebsocketClient {
  private final SymbolVariableService symbolVariableService;
  private final BinancePublicWebsocketHandler binancePublicWebsocketHandler;
  private List<String> symbols;
  private static String WS_URL = "wss://fstream.binance.com/stream?streams=";
  private static boolean isRunning = false;

  private WebSocketSession session;

  @PostConstruct
  private void init() {
    symbols = symbolVariableService.getSupportedSymbolNames();
  }

  private void reconnect() {
    session = null;
    isRunning = false;

    connect();
  }

  @Override
  public void connect() {
    if (isRunning) {
      throw new IllegalStateException("Binance WebSocket is already running!");
    }
    try {
      StandardWebSocketClient client = new StandardWebSocketClient();
      String params = createStreamParams();
      WS_URL = WS_URL.concat(params);
      session =
          client
              .execute(
                  binancePublicWebsocketHandler
                      .setMessageHandler(this::handleMessage)
                      .setReconnectHandler(this::reconnect),
                  WS_URL)
              .get();
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
      } catch (IOException e) {
        log.error("Error closing Binance WebSocket", e);
      }
    }
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
    String symbol = data.get("s").asText().toUpperCase().replace("USDT", "");

    TradeDto trade =
        TradeDto.builder()
            .symbol(symbol)
            .price(Double.parseDouble(data.get("p").asText()))
            .timestamp(data.get("T").asLong())
            .build();

    tradeMap.put(symbol, trade);
  }

  @Override
  protected void handleOrderbook(JsonNode data) {
    String symbol = data.get("s").asText().toUpperCase().replace("USDT", "");

    OrderbookDto orderbook =
        OrderbookDto.builder()
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
      orderbooksUnits[i] =
          OrderbookDto.OrderbookUnit.builder()
              .price(Double.parseDouble(unit.get(0).asText()))
              .size(Double.parseDouble(unit.get(1).asText()))
              .build();
    }
    return orderbooksUnits;
  }

  private String createStreamParams() {
    return Stream.concat(
            symbols.stream().map(symbol -> symbol.toLowerCase() + "usdt" + "@aggTrade"),
            symbols.stream().map(symbol -> symbol.toLowerCase() + "usdt" + "@depth10@100ms"))
        .collect(Collectors.joining("/"));
  }
}
