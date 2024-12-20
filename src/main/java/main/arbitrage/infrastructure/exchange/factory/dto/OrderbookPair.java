package main.arbitrage.infrastructure.exchange.factory.dto;

import lombok.Getter;

@Getter
public class OrderbookPair {
    private final OrderbookDto upbit;
    private final OrderbookDto binance;

    public OrderbookPair(OrderbookDto upbit, OrderbookDto binance) {
        this.upbit = upbit;
        this.binance = binance;
    }
}
