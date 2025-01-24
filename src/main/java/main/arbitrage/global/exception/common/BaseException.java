package main.arbitrage.global.exception.common;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
  private final BaseErrorCode errorCode;
  private String serverMessage = "";

  protected BaseException(BaseErrorCode errorCode, String serverMessage, Throwable cause) {
    super(
        String.format(
            "[%s] %s - %s", errorCode.getCode(), errorCode.getClientMessage(), serverMessage),
        cause);
    this.errorCode = errorCode;
    this.serverMessage = serverMessage;
  }

  protected BaseException(BaseErrorCode errorCode, Throwable cause) {
    super(String.format("[%s] %s", errorCode.getCode(), errorCode.getClientMessage()), cause);
    this.errorCode = errorCode;
    this.serverMessage = cause.getMessage();
  }

  protected BaseException(BaseErrorCode errorCode, String serverMessage) {
    super(
        String.format(
            "[%s] %s - %s", errorCode.getCode(), errorCode.getClientMessage(), serverMessage),
        new Throwable());
    this.errorCode = errorCode;
    this.serverMessage = serverMessage;
  }

  protected BaseException(BaseErrorCode errorCode) {
    super(
        String.format("[%s] %s", errorCode.getCode(), errorCode.getClientMessage()),
        new Throwable());
    this.errorCode = errorCode;
  }
}
