package main.arbitrage.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements BaseErrorCode {
  UNKNOWN("G01", "알 수 없는 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  EXCHANGE_RATE_NULL("G02", "서버 내부 에러입니다. 잠시 뒤 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
