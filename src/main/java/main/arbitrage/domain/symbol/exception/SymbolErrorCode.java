package main.arbitrage.domain.symbol.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SymbolErrorCode implements BaseErrorCode {
  UNKNOWN("S01", "알 수 없는 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  EMPTY_SYMBOL("S02", "심볼이 비어있습니다.", HttpStatus.BAD_REQUEST),
  NOT_FOUND_SYMBOL("S03", "지원하지 않는 심볼입니다.", HttpStatus.BAD_REQUEST),
  INITIALIZED_FAILED("S04", "심볼 초기화 실패.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
