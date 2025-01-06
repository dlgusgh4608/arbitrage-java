package main.arbitrage.domain.sellOrder.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SellOrderErrorCode implements BaseErrorCode {
  UNKNOWN("SO01", "알 수 없는 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_QTY("SO02", "개수를 입력해주세요.", HttpStatus.BAD_REQUEST),
  QTY_TO_LARGER("SO03", "판매 가능한 개수보다 더 많이 판매 할 수 없습니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
