package main.arbitrage.infrastructure.websocket;

public interface WebSocketClient {
    void connect();

    void disconnect();

    boolean isConnected();
}