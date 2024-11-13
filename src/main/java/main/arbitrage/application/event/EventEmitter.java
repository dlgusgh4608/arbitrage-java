package main.arbitrage.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class EventEmitter {
    private final Map<String, Set<EventListener>> listeners = new ConcurrentHashMap<>();

    public void on(String eventName, EventListener listener) {
        listeners.computeIfAbsent(eventName, k -> new CopyOnWriteArraySet<>())
                .add(listener);
    }

    public void emit(String eventName, JsonNode data) {
        Set<EventListener> eventListeners = listeners.get(eventName);
        if (eventListeners != null) {
            eventListeners.forEach(listener -> listener.onEvent(data));
        }
    }

    public void off(String eventName, EventListener listener) {
        Set<EventListener> eventListeners = listeners.get(eventName);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }
}