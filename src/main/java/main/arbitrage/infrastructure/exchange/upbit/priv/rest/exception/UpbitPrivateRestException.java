package main.arbitrage.infrastructure.exchange.upbit.priv.rest.exception;

import lombok.Getter;

@Getter
public class UpbitPrivateRestException extends RuntimeException {
    private final String errorCode;

    public UpbitPrivateRestException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
