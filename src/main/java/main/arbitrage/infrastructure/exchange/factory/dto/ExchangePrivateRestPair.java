package main.arbitrage.infrastructure.exchange.factory.dto;

import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;

public class ExchangePrivateRestPair {
    private final UpbitPrivateRestService upbit;
    private final BinancePrivateRestService binance;

    public ExchangePrivateRestPair(UpbitPrivateRestService upbit, BinancePrivateRestService binance) {
        this.upbit = upbit;
        this.binance = binance;
    }

    public UpbitPrivateRestService getUpbit() {
        return upbit;
    }

    public BinancePrivateRestService getBinance() {
        return binance;
    }
}