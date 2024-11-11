package main.arbitrage.domain.symbolPrice.controller;

import lombok.Getter;
import main.arbitrage.domain.symbolPrice.dto.TradeDto;

@Getter
public class ExchangePair {
    private final TradeDto domestic;
    private final TradeDto overseas;

    public ExchangePair(TradeDto domestic, TradeDto overseas) {
        this.domestic = domestic;
        this.overseas = overseas;
    }
}