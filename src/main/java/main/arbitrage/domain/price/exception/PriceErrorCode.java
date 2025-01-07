package main.arbitrage.domain.price.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PriceErrorCode implements BaseErrorCode {
  UNKNOWN("PR01", "알 수 없는 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  PRICE_NULL("PR02", "서버 내부 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
