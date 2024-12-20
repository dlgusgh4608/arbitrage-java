package main.arbitrage.infrastructure.websocket.server.handler;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.application.collector.dto.PremiumDTO;
import main.arbitrage.infrastructure.websocket.server.BaseServerSocketHandler;

@Component
public class PremiumServerWebSocketHandler extends BaseServerSocketHandler<PremiumDTO> {
    public PremiumServerWebSocketHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}
