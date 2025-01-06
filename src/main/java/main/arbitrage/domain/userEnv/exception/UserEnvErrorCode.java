package main.arbitrage.domain.userEnv.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserEnvErrorCode implements BaseErrorCode {
  UNKNOWN("UE01", "알 수 없는 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  NOT_FOUND_USER_ENV("UE02", "API키를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
