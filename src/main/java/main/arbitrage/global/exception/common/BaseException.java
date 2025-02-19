package main.arbitrage.global.exception.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class BaseException extends RuntimeException {
  private final BaseErrorCode errorCode;
  private String serverMessage = "";

  protected BaseException(BaseErrorCode errorCode, String serverMessage, Throwable cause) {
    super(String.format("[%s] %s", errorCode.getCode(), errorCode.getClientMessage()), cause);

    log.error(
        "[{}] {} - {}", errorCode.getCode(), errorCode.getClientMessage(), serverMessage, cause);
    this.errorCode = errorCode;
    this.serverMessage = serverMessage;
  }

  protected BaseException(BaseErrorCode errorCode, Throwable cause) {
    super(String.format("[%s] %s", errorCode.getCode(), errorCode.getClientMessage()), cause);

    log.error("[{}] {} - {}", errorCode.getCode(), errorCode.getClientMessage(), cause);
    this.errorCode = errorCode;
  }

  protected BaseException(BaseErrorCode errorCode, String serverMessage) {
    super(
        String.format("[%s] %s", errorCode.getCode(), errorCode.getClientMessage()),
        new Throwable());

    log.error("[{}] {} - {}", errorCode.getCode(), errorCode.getClientMessage(), serverMessage);
    this.errorCode = errorCode;
    this.serverMessage = serverMessage;
  }

  protected BaseException(BaseErrorCode errorCode) {
    super(
        String.format("[%s] %s", errorCode.getCode(), errorCode.getClientMessage()),
        new Throwable());

    log.error("[{}] {} - {}", errorCode.getCode(), errorCode.getClientMessage());
    this.errorCode = errorCode;
  }
}
