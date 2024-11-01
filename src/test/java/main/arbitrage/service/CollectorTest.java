package main.arbitrage.service;

import main.arbitrage.domain.collector.infrastructure.websocket.common.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CollectorTest {

    @Mock
    private WebSocketClient upbitWebSocket;

    @InjectMocks
    private Collector collector;

    @Test
    void run_ShouldConnectWebSocket() {
        // when
        collector.run();

        // then
        verify(upbitWebSocket).connect();
    }
}