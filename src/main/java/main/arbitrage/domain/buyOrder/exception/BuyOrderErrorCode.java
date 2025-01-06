package main.arbitrage.domain.buyOrder.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BuyOrderErrorCode implements BaseErrorCode {
  UNKNOWN("BO01", "알 수 없는 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  NOT_FOUND("BO02", "주문을 찾을 수 없습니다.", HttpStatus.CONFLICT);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
