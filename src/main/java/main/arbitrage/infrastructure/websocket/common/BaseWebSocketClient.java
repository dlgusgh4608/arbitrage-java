package main.arbitrage.infrastructure.websocket.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.infrastructure.websocket.common.dto.CommonOrderbookDto;
import main.arbitrage.infrastructure.websocket.common.dto.CommonTradeDto;

import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseWebSocketClient implements WebSocketClient {
    protected final ConcurrentHashMap<String, CommonTradeDto> tradeMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, CommonOrderbookDto> orderbookMap = new ConcurrentHashMap<>();
    protected ObjectMapper objectMapper;

    public BaseWebSocketClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected abstract void handleMessage(JsonNode message);

    protected abstract void handleTrade(JsonNode data);

    protected abstract void handleOrderbook(JsonNode data);

    public CommonTradeDto getTrade(String symbol) {
        return tradeMap.get(symbol);
    }

    public CommonOrderbookDto getOrderbook(String symbol) {
        return orderbookMap.get(symbol);
    }
}