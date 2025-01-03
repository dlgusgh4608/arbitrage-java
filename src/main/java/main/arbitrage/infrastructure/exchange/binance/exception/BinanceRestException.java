package main.arbitrage.infrastructure.exchange.binance.exception;

import lombok.Getter;

@Getter
public class BinanceRestException extends RuntimeException {
  private final String errorCode;

  public BinanceRestException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
  }
}
