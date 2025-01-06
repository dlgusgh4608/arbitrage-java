package main.arbitrage.infrastructure.exchange.upbit.exception;

import main.arbitrage.global.exception.common.BaseException;

public class UpbitException extends BaseException {

  public UpbitException(UpbitErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public UpbitException(UpbitErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public UpbitException(UpbitErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public UpbitException(UpbitErrorCode errorCode) {
    super(errorCode);
  }
}
