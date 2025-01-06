package main.arbitrage.domain.sellOrder.exception;

import main.arbitrage.global.exception.common.BaseException;

public class SellOrderException extends BaseException {

  public SellOrderException(SellOrderErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public SellOrderException(SellOrderErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public SellOrderException(SellOrderErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public SellOrderException(SellOrderErrorCode errorCode) {
    super(errorCode);
  }
}
