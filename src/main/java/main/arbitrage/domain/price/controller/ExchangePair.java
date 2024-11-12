package main.arbitrage.domain.price.controller;

import lombok.Getter;
import main.arbitrage.domain.price.dto.TradeDto;

@Getter
public class ExchangePair {
    private final TradeDto upbit;
    private final TradeDto binance;

    public ExchangePair(TradeDto upbit, TradeDto binance) {
        this.upbit = upbit;
        this.binance = binance;
    }
}