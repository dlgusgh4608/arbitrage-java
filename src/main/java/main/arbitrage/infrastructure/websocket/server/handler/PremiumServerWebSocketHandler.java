package main.arbitrage.infrastructure.websocket.server.handler;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.infrastructure.event.dto.PremiumDto;
import main.arbitrage.infrastructure.websocket.server.BaseServerSocketHandler;

@Component
public class PremiumServerWebSocketHandler extends BaseServerSocketHandler<PremiumDto> {
    public PremiumServerWebSocketHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}
