package main.arbitrage.infrastructure.oauthValidator.exception;

import main.arbitrage.global.exception.common.BaseException;

public class OauthValidatorException extends BaseException {

  public OauthValidatorException(
      OauthValidatorErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public OauthValidatorException(OauthValidatorErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public OauthValidatorException(OauthValidatorErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public OauthValidatorException(OauthValidatorErrorCode errorCode) {
    super(errorCode);
  }
}
