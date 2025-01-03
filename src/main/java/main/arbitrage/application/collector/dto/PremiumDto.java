package main.arbitrage.application.collector.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PremiumDTO {
    private final String symbol;
    private final double premium;
    private final double upbit;
    private final double binance;
    private final double usdToKrw;
    private final Long upbitTradeAt;
    private final Long binanceTradeAt;
}
