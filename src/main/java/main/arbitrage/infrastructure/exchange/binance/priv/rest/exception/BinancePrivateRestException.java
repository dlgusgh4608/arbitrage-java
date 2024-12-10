package main.arbitrage.infrastructure.exchange.binance.priv.rest.exception;

import lombok.Getter;

@Getter
public class BinancePrivateRestException extends RuntimeException {
    private final String errorCode;

    public BinancePrivateRestException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}