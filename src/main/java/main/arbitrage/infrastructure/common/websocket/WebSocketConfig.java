package main.arbitrage.infrastructure.common.websocket;

import lombok.RequiredArgsConstructor;
import main.arbitrage.infrastructure.common.websocket.handler.ClientWebSocketHandler;
import main.arbitrage.infrastructure.common.websocket.handler.ExchangeWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ClientWebSocketHandler handler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "ws/premium").setAllowedOrigins("*");
    }
}