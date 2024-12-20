package main.arbitrage.infrastructure.exchange.factory;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import main.arbitrage.infrastructure.exchange.binance.pub.websocket.BinanceWebSocket;
import main.arbitrage.infrastructure.exchange.dto.OrderbookPair;
import main.arbitrage.infrastructure.exchange.dto.TradePair;
import main.arbitrage.infrastructure.exchange.upbit.pub.websocket.UpbitWebSocket;

@Component
@RequiredArgsConstructor
public class ExchangePublicWebsocketFactory {
    private final UpbitWebSocket upbit;
    private final BinanceWebSocket binance;

    public void initialize() {
        upbit.connect();
        binance.connect();
    }

    public TradePair collectTrades(String symbol) {
        return new TradePair(upbit.getTrade(symbol), binance.getTrade(symbol));
    }

    public OrderbookPair collectOrderbooks(String symbol) {
        return new OrderbookPair(upbit.getOrderbook(symbol), binance.getOrderbook(symbol));
    }
}
