package main.arbitrage.infrastructure.event.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class PremiumDto {
    private String symbol;
    private BigDecimal premium;
    private BigDecimal domestic;
    private BigDecimal overseas;
    private double usdToKrw;
    private Long domesticTradeAt;
    private Long overseasTradeAt;
}