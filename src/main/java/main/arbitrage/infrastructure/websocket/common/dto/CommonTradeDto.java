package main.arbitrage.infrastructure.websocket.common.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CommonTradeDto {
    private String symbol;
    private BigDecimal price;
    private long timestamp;
}