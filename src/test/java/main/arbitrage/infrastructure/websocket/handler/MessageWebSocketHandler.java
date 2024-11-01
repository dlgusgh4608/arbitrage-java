package main.arbitrage.infrastructure.websocket.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.domain.collector.infrastructure.websocket.handler.MessageWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageWebSocketHandlerTest {

    @Mock
    private WebSocketSession session;

    @Mock
    private Consumer<JsonNode> messageHandler;

    private MessageWebSocketHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new MessageWebSocketHandler("Test", messageHandler);
    }

    @Test
    void handleMessage_WithValidJson_ShouldCallMessageHandler() throws Exception {
        // given
        String jsonMessage = "{\"type\":\"trade\",\"code\":\"KRW-BTC\"}";
        TextMessage message = new TextMessage(jsonMessage);

        // when
        handler.handleMessage(session, message);

        // then
        verify(messageHandler).accept(any(JsonNode.class));
    }
}