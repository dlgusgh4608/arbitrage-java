package main.arbitrage.infrastructure.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.infrastructure.websocket.common.BaseWebsocketHandler;
import org.springframework.stereotype.Component;

@Component
public class UpbitPublicWebsocketHandler extends BaseWebsocketHandler<Object> {
  public UpbitPublicWebsocketHandler(ObjectMapper objectMapper) {
    super("Upbit Public", objectMapper);
  }
}
