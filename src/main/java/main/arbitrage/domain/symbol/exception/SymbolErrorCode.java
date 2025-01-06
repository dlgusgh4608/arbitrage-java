package main.arbitrage.domain.symbol.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SymbolErrorCode implements BaseErrorCode {
  UNKNOWN("S01", "알 수 없는 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  NOT_FOUND_SYMBOL("S02", "지원하지 않는 심볼입니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
