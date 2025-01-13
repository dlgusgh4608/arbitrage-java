package main.arbitrage.infrastructure.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.application.collector.dto.PremiumDTO;
import main.arbitrage.infrastructure.websocket.common.BaseWebsocketHandler;
import org.springframework.stereotype.Component;

@Component
public class PremiumWebsocketHandler extends BaseWebsocketHandler<PremiumDTO> {
  public PremiumWebsocketHandler(ObjectMapper objectMapper) {
    super("Premium to Client", objectMapper);
  }
}
