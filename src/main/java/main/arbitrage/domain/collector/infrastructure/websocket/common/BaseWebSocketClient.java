package main.arbitrage.domain.collector.infrastructure.websocket.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.domain.collector.dto.TradeDto;
import main.arbitrage.domain.collector.dto.OrderbookDto;

import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseWebSocketClient implements WebSocketClient {
    protected final ConcurrentHashMap<String, TradeDto> tradeMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, OrderbookDto> orderbookMap = new ConcurrentHashMap<>();
    protected ObjectMapper objectMapper;

    public BaseWebSocketClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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