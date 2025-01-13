package main.arbitrage.infrastructure.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.infrastructure.websocket.common.BaseWebsocketHandler;
import org.springframework.stereotype.Component;

@Component
public class BinancePublicWebsocketHandler extends BaseWebsocketHandler<Object> {
  public BinancePublicWebsocketHandler(ObjectMapper objectMapper) {
    super("Binance Public", objectMapper);
  }
}
