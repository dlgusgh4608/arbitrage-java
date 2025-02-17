package main.arbitrage.infrastructure.exchanges.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderbookPair {
  private final OrderbookDto upbit;
  private final OrderbookDto binance;
}
