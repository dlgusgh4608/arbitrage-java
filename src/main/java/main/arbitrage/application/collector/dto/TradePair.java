package main.arbitrage.application.collector.dto;

import lombok.Getter;

@Getter
public class TradePair {
    private final TradeDto upbit;
    private final TradeDto binance;

    public TradePair(TradeDto upbit, TradeDto binance) {
        this.upbit = upbit;
        this.binance = binance;
    }
}