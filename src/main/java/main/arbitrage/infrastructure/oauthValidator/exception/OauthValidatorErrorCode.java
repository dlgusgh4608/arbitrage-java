package main.arbitrage.infrastructure.oauthValidator.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OauthValidatorErrorCode implements BaseErrorCode {
  UNKNOWN("OV01", "알 수 없는 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  UN_SUCCESS("OV02", "OAuth검증에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_PROVIDER("OV03", "지원하지 않는 플랫폼입니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
