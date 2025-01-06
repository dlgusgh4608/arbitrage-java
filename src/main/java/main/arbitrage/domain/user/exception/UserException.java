package main.arbitrage.domain.user.exception;

import main.arbitrage.global.exception.common.BaseException;

public class UserException extends BaseException {

  public UserException(UserErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public UserException(UserErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public UserException(UserErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public UserException(UserErrorCode errorCode) {
    super(errorCode);
  }
}
