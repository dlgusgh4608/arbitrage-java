package main.arbitrage.infrastructure.email.exception;

import main.arbitrage.global.exception.common.BaseException;

public class SendMailException extends BaseException {

  public SendMailException(SendMailErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public SendMailException(SendMailErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public SendMailException(SendMailErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public SendMailException(SendMailErrorCode errorCode) {
    super(errorCode);
  }
}
