package main.arbitrage.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
  UNKNOWN("AT01", "Unauthorized", HttpStatus.UNAUTHORIZED),
  TOKEN_NOT_FOUND("AT02", "Unauthorized", HttpStatus.UNAUTHORIZED),
  INVALID_TOKEN("AT03", "Unauthorized", HttpStatus.UNAUTHORIZED),
  EXPIRED_TOKEN("AT04", "Unauthorized", HttpStatus.UNAUTHORIZED),
  REFRESH_TOKEN_NOT_FOUND("AT05", "Unauthorized", HttpStatus.UNAUTHORIZED),
  REFRESH_TOKEN_EXPIRED("AT06", "Unauthorized", HttpStatus.UNAUTHORIZED),
  REFRESH_TOKEN_MISMATCH("AT07", "Unauthorized", HttpStatus.UNAUTHORIZED),
  UN_AUTHENTICATED("AT08", "Unauthorized", HttpStatus.UNAUTHORIZED),
  OAUTH_SERIALIZE("AT09", "Unauthorized", HttpStatus.UNAUTHORIZED),
  OAUTH_DESERIALIZE("AT10", "Unauthorized", HttpStatus.UNAUTHORIZED);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
