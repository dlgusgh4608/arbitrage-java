package main.arbitrage.infrastructure.exchange.binance.exception;

import main.arbitrage.global.exception.BaseException;

public class BinanceException extends BaseException {

  public BinanceException(BinanceErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public BinanceException(BinanceErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public BinanceException(BinanceErrorCode errorCode) {
    super(errorCode);
  }
}