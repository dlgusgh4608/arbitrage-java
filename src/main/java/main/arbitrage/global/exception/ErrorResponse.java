package main.arbitrage.global.exception;

import lombok.Builder;
import lombok.Getter;
import main.arbitrage.global.exception.common.BaseException;

@Getter
@Builder
public class ErrorResponse {
  private final String code;
  private final String message;

  public static ErrorResponse of(BaseException e) {
    return ErrorResponse.builder()
        .code(e.getErrorCode().getCode())
        .message(e.getErrorCode().getClientMessage())
        .build();
  }
}
