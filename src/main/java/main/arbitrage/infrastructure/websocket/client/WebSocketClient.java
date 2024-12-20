package main.arbitrage.infrastructure.websocket.client;

public interface WebSocketClient {
    void connect();

    void disconnect();

    boolean isConnected();
}
