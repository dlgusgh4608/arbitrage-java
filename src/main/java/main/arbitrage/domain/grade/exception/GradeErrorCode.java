package main.arbitrage.domain.grade.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GradeErrorCode implements BaseErrorCode {
  UNKNOWN("G01", "알 수 없는 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  INITIALIZED_FAILED("G02", "등급 초기화 실패", HttpStatus.INTERNAL_SERVER_ERROR),
  NOT_FOUND("G03", "없는 등급입니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
