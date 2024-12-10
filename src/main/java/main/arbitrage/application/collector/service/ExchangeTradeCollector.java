package main.arbitrage.application.collector.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.application.collector.dto.OrderbookPair;
import main.arbitrage.application.collector.dto.TradePair;
import main.arbitrage.infrastructure.exchange.binance.pub.websocket.BinanceWebSocket;
import main.arbitrage.infrastructure.exchange.upbit.pub.websocket.UpbitWebSocket;
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

    public TradePair collectTrades(String symbol) {
        return new TradePair(
                upbit.getTrade(symbol),
                binance.getTrade(symbol)
        );
    }

    public OrderbookPair collectOrderbooks(String symbol) {
        return new OrderbookPair(
                upbit.getOrderbook(symbol),
                binance.getOrderbook(symbol)
        );
    }
}