package main.arbitrage.domain.price.controller;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.price.infrastructure.websocket.exchange.binance.BinanceWebSocket;
import main.arbitrage.domain.price.infrastructure.websocket.exchange.upbit.UpbitWebSocket;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExchangeTradeCollector {
    private final UpbitWebSocket upbit;
    private final BinanceWebSocket binance;

    public void initialize() {
        upbit.connect();
        binance.connect();
    }

    public ExchangePair collectTrades(String symbol) {
        return new ExchangePair(
                upbit.getTrade(symbol),
                binance.getTrade(symbol)
        );
    }
}