package main.arbitrage.domain.collector.infrastructure.websocket.common;

public interface WebSocketClient {
    void connect();

    void disconnect();

    boolean isConnected();
}