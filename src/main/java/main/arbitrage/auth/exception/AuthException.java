package main.arbitrage.auth.exception;

import main.arbitrage.global.exception.common.BaseException;

public class AuthException extends BaseException {

  public AuthException(AuthErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public AuthException(AuthErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public AuthException(AuthErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public AuthException(AuthErrorCode errorCode) {
    super(errorCode);
  }
}
