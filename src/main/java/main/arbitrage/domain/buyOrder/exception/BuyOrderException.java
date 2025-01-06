package main.arbitrage.domain.buyOrder.exception;

import main.arbitrage.global.exception.common.BaseException;

public class BuyOrderException extends BaseException {

  public BuyOrderException(BuyOrderErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public BuyOrderException(BuyOrderErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public BuyOrderException(BuyOrderErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public BuyOrderException(BuyOrderErrorCode errorCode) {
    super(errorCode);
  }
}
