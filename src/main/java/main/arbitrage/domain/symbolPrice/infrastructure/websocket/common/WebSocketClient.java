package main.arbitrage.domain.symbolPrice.infrastructure.websocket.common;

public interface WebSocketClient {
    void connect();

    void disconnect();

    boolean isConnected();
}