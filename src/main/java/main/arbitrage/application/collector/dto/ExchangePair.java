package main.arbitrage.application.collector.dto;

import lombok.Getter;

@Getter
public class ExchangePair {
    private final TradeDto upbit;
    private final TradeDto binance;

    public ExchangePair(TradeDto upbit, TradeDto binance) {
        this.upbit = upbit;
        this.binance = binance;
    }
}