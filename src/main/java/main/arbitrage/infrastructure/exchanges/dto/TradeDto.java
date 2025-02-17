package main.arbitrage.infrastructure.exchanges.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TradeDto {
  private final String symbol;
  private final double price;
  private final long timestamp;
}
