package main.arbitrage.domain.collector.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TradeDto {
    private String symbol;
    private BigDecimal price;
    private long timestamp;
}