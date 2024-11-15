package main.arbitrage.infrastructure.common;

public interface WebSocketClient {
    void connect();

    void disconnect();

    boolean isConnected();
}