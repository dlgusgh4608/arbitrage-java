package main.arbitrage.infrastructure.websocket.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.ConcurrentHashMap;
import main.arbitrage.infrastructure.exchange.dto.OrderbookDto;
import main.arbitrage.infrastructure.exchange.dto.TradeDto;

public abstract class BaseWebSocketClient implements WebSocketClient {
  protected final ConcurrentHashMap<String, TradeDto> tradeMap = new ConcurrentHashMap<>();
  protected final ConcurrentHashMap<String, OrderbookDto> orderbookMap = new ConcurrentHashMap<>();

  protected abstract void handleMessage(JsonNode message);

  protected abstract void handleTrade(JsonNode data);

  protected abstract void handleOrderbook(JsonNode data);

  public TradeDto getTrade(String symbol) {
    return tradeMap.get(symbol);
  }

  public OrderbookDto getOrderbook(String symbol) {
    return orderbookMap.get(symbol);
  }
}
