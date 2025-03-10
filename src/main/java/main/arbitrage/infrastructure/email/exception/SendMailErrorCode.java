package main.arbitrage.infrastructure.email.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SendMailErrorCode implements BaseErrorCode {
  UNKNOWN("SM01", "알 수 없는 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_MAIL("SM02", "메일 주소가 잘못되었습니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
