package main.arbitrage.domain.price.exception;

import main.arbitrage.global.exception.common.BaseException;

public class PriceException extends BaseException {

  public PriceException(PriceErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public PriceException(PriceErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public PriceException(PriceErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public PriceException(PriceErrorCode errorCode) {
    super(errorCode);
  }
}
