package main.arbitrage.infrastructure.exchange.factory;

import lombok.RequiredArgsConstructor;
import main.arbitrage.infrastructure.exchange.binance.pub.websocket.BinanceWebSocket;
import main.arbitrage.infrastructure.exchange.dto.OrderbookPair;
import main.arbitrage.infrastructure.exchange.dto.TradePair;
import main.arbitrage.infrastructure.exchange.upbit.pub.websocket.UpbitWebSocket;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangePublicWebsocketFactory {
  private final UpbitWebSocket upbit;
  private final BinanceWebSocket binance;

  public void initialize() {
    upbit.connect();
    binance.connect();
  }

  public TradePair collectTrades(String symbol) {
    return TradePair.builder()
        .upbit(upbit.getTrade(symbol))
        .binance(binance.getTrade(symbol))
        .build();
  }

  public OrderbookPair collectOrderbooks(String symbol) {
    return OrderbookPair.builder()
        .upbit(upbit.getOrderbook(symbol))
        .binance(binance.getOrderbook(symbol))
        .build();
  }
}
