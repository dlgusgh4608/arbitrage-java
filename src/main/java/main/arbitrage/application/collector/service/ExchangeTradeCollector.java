package main.arbitrage.application.collector.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.application.collector.dto.ExchangePair;
import main.arbitrage.infrastructure.binance.pub.websocket.BinanceWebSocket;
import main.arbitrage.infrastructure.upbit.pub.websocket.UpbitWebSocket;
import org.springframework.stereotype.Service;

@Service
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