package main.arbitrage.service;


import main.arbitrage.infrastructure.websocket.common.WebSocketClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class Collector {
    private final WebSocketClient upbitWebSocket;
    private final WebSocketClient binanceWebSocket;

    public Collector(
            @Qualifier("upbitWebSocket") WebSocketClient upbitWebSocket,
            @Qualifier("binanceWebSocket") WebSocketClient binanceWebSocket
    ) {
        this.upbitWebSocket = upbitWebSocket;
        this.binanceWebSocket = binanceWebSocket;
    }

    public void run() {
        upbitWebSocket.connect();
        binanceWebSocket.connect();
    }
}