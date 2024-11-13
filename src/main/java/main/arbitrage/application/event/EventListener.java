package main.arbitrage.application.event;

import com.fasterxml.jackson.databind.JsonNode;

@FunctionalInterface
public interface EventListener {
    void onEvent(JsonNode data);
}