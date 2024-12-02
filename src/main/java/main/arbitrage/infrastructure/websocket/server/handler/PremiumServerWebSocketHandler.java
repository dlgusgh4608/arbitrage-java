package main.arbitrage.infrastructure.websocket.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.infrastructure.event.dto.PremiumDto;
import main.arbitrage.infrastructure.websocket.server.BaseServerSocketHandler;
import org.springframework.stereotype.Component;

@Component
public class PremiumServerWebSocketHandler extends BaseServerSocketHandler<PremiumDto> {
    public PremiumServerWebSocketHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}