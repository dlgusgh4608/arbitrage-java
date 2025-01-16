package main.arbitrage.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
  UNKNOWN("U01", "알 수 없는 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  USED_EMAIL("U02", "사용중인 이메일 입니다.", HttpStatus.CONFLICT),
  INVALID_PASSWORD("U03", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
  NOT_FOUND_USER("U04", "사용자를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
  USED_NICKNAME("U05", "사용중인 닉네임 입니다.", HttpStatus.CONFLICT),
  USED_FORBIDDEN("U06", "권한이 없습니다.", HttpStatus.FORBIDDEN);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
