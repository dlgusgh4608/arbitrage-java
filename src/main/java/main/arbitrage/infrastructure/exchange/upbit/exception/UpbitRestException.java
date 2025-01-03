package main.arbitrage.infrastructure.exchange.upbit.exception;

import lombok.Getter;

@Getter
public class UpbitRestException extends RuntimeException {
  private final String errorCode;

  public UpbitRestException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
  }
}
