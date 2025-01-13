package main.arbitrage.config;

import lombok.RequiredArgsConstructor;
import main.arbitrage.infrastructure.websocket.handler.ChartWebsocketHandler;
import main.arbitrage.infrastructure.websocket.handler.PremiumWebsocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
  private final ChartWebsocketHandler chartWebsocketHandler;
  private final PremiumWebsocketHandler premiumWebsocketHandler;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(premiumWebsocketHandler, "ws/premium").setAllowedOrigins("*");
    registry.addHandler(chartWebsocketHandler, "ws/chart/{symbol}").setAllowedOrigins("*");
  }
}
