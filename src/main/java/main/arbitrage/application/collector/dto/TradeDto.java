package main.arbitrage.application.collector.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TradeDto {
    private String symbol;
    private double price;
    private long timestamp;
}