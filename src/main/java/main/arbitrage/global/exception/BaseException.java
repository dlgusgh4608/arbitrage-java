package main.arbitrage.global.exception;

import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {
  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
  private final LocalDateTime timestamp;

  private String serverMessage = "";

  protected BaseException(BaseErrorCode errorCode, String serverMessage, Throwable cause) {
    super(
        String.format(
            "[%s] %s - %s", errorCode.getCode(), errorCode.getClientMessage(), serverMessage),
        cause);

    this.code = errorCode.getCode();
    this.clientMessage = errorCode.getClientMessage();
    this.httpStatus = errorCode.getHttpStatus();
    this.serverMessage = serverMessage;
    this.timestamp = LocalDateTime.now();
  }

  protected BaseException(BaseErrorCode errorCode, String serverMessage) {
    super(
        String.format(
            "[%s] %s - %s", errorCode.getCode(), errorCode.getClientMessage(), serverMessage));

    this.code = errorCode.getCode();
    this.clientMessage = errorCode.getClientMessage();
    this.httpStatus = errorCode.getHttpStatus();
    this.serverMessage = serverMessage;
    this.timestamp = LocalDateTime.now();
  }

  protected BaseException(BaseErrorCode errorCode) {
    super(String.format("[%s] %s", errorCode.getCode(), errorCode.getClientMessage()));

    this.code = errorCode.getCode();
    this.clientMessage = errorCode.getClientMessage();
    this.httpStatus = errorCode.getHttpStatus();
    this.timestamp = LocalDateTime.now();
  }
}
