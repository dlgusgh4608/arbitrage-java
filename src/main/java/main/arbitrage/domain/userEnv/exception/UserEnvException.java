package main.arbitrage.domain.userEnv.exception;

import main.arbitrage.global.exception.common.BaseException;

public class UserEnvException extends BaseException {

  public UserEnvException(UserEnvErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public UserEnvException(UserEnvErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public UserEnvException(UserEnvErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public UserEnvException(UserEnvErrorCode errorCode) {
    super(errorCode);
  }
}
