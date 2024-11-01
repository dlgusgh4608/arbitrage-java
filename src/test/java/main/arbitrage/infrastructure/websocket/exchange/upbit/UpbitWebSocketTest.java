package main.arbitrage.infrastructure.websocket.exchange.upbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.domain.collector.infrastructure.websocket.exchange.upbit.UpbitWebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpbitWebSocketTest {

    @Mock
    private WebSocketSession session;

    @Mock
    private StandardWebSocketClient webSocketClient;

    private ObjectMapper objectMapper;
    private UpbitWebSocket upbitWebSocket;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        upbitWebSocket = new UpbitWebSocket(objectMapper);
        // private 필드에 mock session 주입
        ReflectionTestUtils.setField(upbitWebSocket, "session", session);
        // static 필드 초기화
        ReflectionTestUtils.setField(upbitWebSocket, "isRunning", false);
    }

    @Test
    void connect_WhenAlreadyRunning_ShouldThrowException() {
        // given
        ReflectionTestUtils.setField(upbitWebSocket, "isRunning", true);

        // when & then
        assertThatThrownBy(() -> upbitWebSocket.connect())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already running");
    }

    @Test
    void disconnect_ShouldCloseSession() throws Exception {
        // given
        when(session.isOpen()).thenReturn(true);
        ReflectionTestUtils.setField(upbitWebSocket, "isRunning", true);

        // when
        upbitWebSocket.disconnect();

        // then
        verify(session).close();
        assertThat(upbitWebSocket.isConnected()).isFalse();
    }

    @Test
    void isConnected_ShouldReturnCorrectState() {
        // given
        when(session.isOpen()).thenReturn(true);
        ReflectionTestUtils.setField(upbitWebSocket, "isRunning", true);

        // then
        assertThat(upbitWebSocket.isConnected()).isTrue();
    }
}