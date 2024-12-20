package main.arbitrage.application.collector.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PremiumDto {
    private String symbol;
    private double premium;
    private double upbit;
    private double binance;
    private double usdToKrw;
    private Long upbitTradeAt;
    private Long binanceTradeAt;
}
