package main.arbitrage.infrastructure.websocket.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.application.collector.dto.PremiumDTO;
import main.arbitrage.infrastructure.websocket.server.BaseServerSocketHandler;
import org.springframework.stereotype.Component;

@Component
public class PremiumServerWebSocketHandler extends BaseServerSocketHandler<PremiumDTO> {
  public PremiumServerWebSocketHandler(ObjectMapper objectMapper) {
    super(objectMapper);
  }
}
