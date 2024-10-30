package main.arbitrage.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.infrastructure.websocket.common.WebSocketClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Collector {
    private final WebSocketClient upbitWebSocket;

    public void run() {
        upbitWebSocket.connect();
    }
}