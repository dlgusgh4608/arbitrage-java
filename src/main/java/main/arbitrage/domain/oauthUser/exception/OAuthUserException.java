package main.arbitrage.domain.oauthUser.exception;

import main.arbitrage.global.exception.common.BaseException;

public class OAuthUserException extends BaseException {

  public OAuthUserException(OAuthUserErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public OAuthUserException(OAuthUserErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public OAuthUserException(OAuthUserErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public OAuthUserException(OAuthUserErrorCode errorCode) {
    super(errorCode);
  }
}
