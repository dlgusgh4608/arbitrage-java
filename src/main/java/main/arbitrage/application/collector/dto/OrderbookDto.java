package main.arbitrage.application.collector.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

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