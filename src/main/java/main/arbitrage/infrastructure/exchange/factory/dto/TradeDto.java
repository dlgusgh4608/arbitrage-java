package main.arbitrage.infrastructure.exchange.factory.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TradeDto {
    private String symbol;
    private double price;
    private long timestamp;
}