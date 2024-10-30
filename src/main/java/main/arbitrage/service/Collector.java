package main.arbitrage.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.infrastructure.websocket.common.BaseWebSocketClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Collector {
    private final BaseWebSocketClient upbitWebSocket;
    private final BaseWebSocketClient binanceWebSocket;
    private final ObjectMapper objectMapper;

    public Collector(
            @Qualifier("upbitWebSocket") BaseWebSocketClient upbitWebSocket,
            @Qualifier("binanceWebSocket") BaseWebSocketClient binanceWebSocket,
            ObjectMapper objectMapper
    ) {
        this.upbitWebSocket = upbitWebSocket;
        this.binanceWebSocket = binanceWebSocket;
        this.objectMapper = objectMapper;
    }

    public void run() {
        upbitWebSocket.connect();
        binanceWebSocket.connect();
    }
}