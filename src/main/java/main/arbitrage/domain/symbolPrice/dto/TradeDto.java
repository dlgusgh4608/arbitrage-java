package main.arbitrage.domain.symbolPrice.dto;

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