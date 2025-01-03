package main.arbitrage.config;

import lombok.RequiredArgsConstructor;
import main.arbitrage.infrastructure.websocket.server.handler.ChartServerWebSocketHandler;
import main.arbitrage.infrastructure.websocket.server.handler.PremiumServerWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

  private final PremiumServerWebSocketHandler premiumServerWebSocketHandler;
  private final ChartServerWebSocketHandler chartServerWebSocketHandler;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(premiumServerWebSocketHandler, "ws/premium").setAllowedOrigins("*");
    registry.addHandler(chartServerWebSocketHandler, "ws/chart/{symbol}").setAllowedOrigins("*");
  }
}
