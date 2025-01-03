package main.arbitrage.infrastructure.exchange.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderbookDto {
  private final String symbol;
  private final OrderbookUnit[] bids;
  private final OrderbookUnit[] asks;

  @Builder
  @Getter
  public static class OrderbookUnit {
    private final double price;
    private final double size;
  }
}
