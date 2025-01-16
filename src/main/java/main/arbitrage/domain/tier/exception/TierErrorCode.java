package main.arbitrage.domain.tier.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum TierErrorCode implements BaseErrorCode {
  UNKNOWN("T01", "알 수 없는 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  INITIALIZED_FAILED("T02", "티어 초기화 실패", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
