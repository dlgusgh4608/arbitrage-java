package main.arbitrage.infrastructure.binance.exception;

import main.arbitrage.global.exception.common.BaseException;

public class BinanceException extends BaseException {

  public BinanceException(BinanceErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public BinanceException(BinanceErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public BinanceException(BinanceErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public BinanceException(BinanceErrorCode errorCode) {
    super(errorCode);
  }
}
