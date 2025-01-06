package main.arbitrage.global.exception;

import main.arbitrage.global.exception.common.BaseException;

public class GlobalException extends BaseException {

  public GlobalException(GlobalErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public GlobalException(GlobalErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public GlobalException(GlobalErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public GlobalException(GlobalErrorCode errorCode) {
    super(errorCode);
  }
}
