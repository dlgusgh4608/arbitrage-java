package main.arbitrage.infrastructure.exchanges;

import lombok.RequiredArgsConstructor;
import main.arbitrage.infrastructure.binance.websocket.BinanceWebSocket;
import main.arbitrage.infrastructure.exchanges.dto.OrderbookPair;
import main.arbitrage.infrastructure.exchanges.dto.TradePair;
import main.arbitrage.infrastructure.upbit.websocket.UpbitWebSocket;
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
