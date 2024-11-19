package main.arbitrage.infrastructure.common.websocket;

public interface WebSocketClient {
    void connect();

    void disconnect();

    boolean isConnected();
}