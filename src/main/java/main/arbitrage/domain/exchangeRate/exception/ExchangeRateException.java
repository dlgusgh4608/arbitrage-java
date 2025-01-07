package main.arbitrage.domain.exchangeRate.exception;

import main.arbitrage.global.exception.common.BaseException;

public class ExchangeRateException extends BaseException {

  public ExchangeRateException(
      ExchangeRateErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public ExchangeRateException(ExchangeRateErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public ExchangeRateException(ExchangeRateErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public ExchangeRateException(ExchangeRateErrorCode errorCode) {
    super(errorCode);
  }
}
