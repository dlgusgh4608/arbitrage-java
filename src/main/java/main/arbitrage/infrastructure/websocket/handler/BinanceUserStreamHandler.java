package main.arbitrage.infrastructure.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.infrastructure.websocket.common.BaseWebsocketHandler;
import org.springframework.stereotype.Component;

@Component
public class BinanceUserStreamHandler extends BaseWebsocketHandler<Object> {
  public BinanceUserStreamHandler(ObjectMapper objectMapper) {
    super("Binance UserStream", objectMapper);
  }

  public BinanceUserStreamHandler setSocketName(String socketName) {
    this.socketName = socketName;
    return this;
  }
}
