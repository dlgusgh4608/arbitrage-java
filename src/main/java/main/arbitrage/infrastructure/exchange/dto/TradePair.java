package main.arbitrage.infrastructure.exchange.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TradePair {
    private final TradeDto upbit;
    private final TradeDto binance;
}
