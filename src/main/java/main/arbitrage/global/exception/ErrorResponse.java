package main.arbitrage.global.exception;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
  private final String code;
  private final String message;
  private final LocalDateTime timestamp;

  public static ErrorResponse of(BaseException baseException) {
    return ErrorResponse.builder()
        .code(baseException.getCode())
        .message(baseException.getClientMessage())
        .timestamp(baseException.getTimestamp())
        .build();
  }
}
