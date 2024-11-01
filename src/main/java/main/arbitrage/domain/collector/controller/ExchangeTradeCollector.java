package main.arbitrage.domain.collector.controller;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.collector.infrastructure.websocket.exchange.binance.BinanceWebSocket;
import main.arbitrage.domain.collector.infrastructure.websocket.exchange.upbit.UpbitWebSocket;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeTradeCollector {
    private final UpbitWebSocket domestic;
    private final BinanceWebSocket overseas;

    public void initialize() {
        domestic.connect();
        overseas.connect();
    }

    public ExchangePair collectTrades(String symbol) {
        return new ExchangePair(
                domestic.getTrade(symbol),
                overseas.getTrade(symbol)
        );
    }
}