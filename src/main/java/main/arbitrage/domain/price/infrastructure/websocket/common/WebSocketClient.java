package main.arbitrage.domain.price.infrastructure.websocket.common;

import java.util.List;

public interface WebSocketClient {
    void connect();

    void disconnect();

    boolean isConnected();
}