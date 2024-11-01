package main.arbitrage.common.event;

import com.fasterxml.jackson.databind.JsonNode;

@FunctionalInterface
public interface EventListener {
    void onEvent(JsonNode data);
}