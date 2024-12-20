package main.arbitrage.infrastructure.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class EventEmitterTest {

    private EventEmitter eventEmitter;
    private ObjectMapper objectMapper;

    @Mock
    private EventListener eventListener;

    @BeforeEach
    void setUp() {
        eventEmitter = new EventEmitter();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldEmitEventToRegisteredListener() {
        // given
        String eventName = "test-event";
        JsonNode data = objectMapper.createObjectNode().put("message", "test");
        eventEmitter.on(eventName, eventListener);

        // when
        eventEmitter.emit(eventName, data);

        // then
        verify(eventListener, times(1)).onEvent(data);
    }

    @Test
    void shouldNotEmitEventToUnregisteredListener() {
        // given
        String eventName = "test-event";
        JsonNode data = objectMapper.createObjectNode().put("message", "test");
        eventEmitter.on(eventName, eventListener);
        eventEmitter.off(eventName, eventListener);

        // when
        eventEmitter.emit(eventName, data);

        // then
        verify(eventListener, never()).onEvent(any());
    }

    @Test
    void shouldEmitEventToMultipleListeners() {
        // given
        String eventName = "test-event";
        JsonNode data = objectMapper.createObjectNode().put("message", "test");
        EventListener secondListener = mock(EventListener.class);

        eventEmitter.on(eventName, eventListener);
        eventEmitter.on(eventName, secondListener);

        // when
        eventEmitter.emit(eventName, data);

        // then
        verify(eventListener, times(1)).onEvent(data);
        verify(secondListener, times(1)).onEvent(data);
    }

    @Test
    void shouldNotEmitEventWhenNoListenersRegistered() {
        // given
        String eventName = "test-event";
        JsonNode data = objectMapper.createObjectNode().put("message", "test");

        // when
        eventEmitter.emit(eventName, data);

        // then
        verify(eventListener, never()).onEvent(any());
    }
}
