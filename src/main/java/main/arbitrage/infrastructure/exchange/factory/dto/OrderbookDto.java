package main.arbitrage.infrastructure.exchange.factory.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderbookDto {
    private String symbol;
    private OrderbookUnit[] bids;
    private OrderbookUnit[] asks;

    @Builder
    @Getter
    public static class OrderbookUnit {
        private double price;
        private double size;
    }
}
