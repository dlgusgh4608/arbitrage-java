package main.arbitrage.infrastructure.exchange.upbit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UpbitErrorCode implements BaseErrorCode {
  UNKNOWN("EU01", "알 수 없는 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_QUERY_PAYLOAD("EU02", "JWT 헤더의 페이로드가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
  JWT_VERIFICATION("EU03", "JWT 헤더 검증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
  EXPIRED_ACCESS_KEY("EU04", "API 키가 만료되었습니다.", HttpStatus.UNAUTHORIZED),
  NONCE_USED("EU04", "이미 사용된 nonce값입니다.", HttpStatus.BAD_REQUEST),
  NO_AUTHORIZATION_IP("EU05", "허용되지 않은 IP 주소입니다.", HttpStatus.UNAUTHORIZED),
  OUT_OF_SCOPE("EU06", "허용되지 않은 기능입니다.", HttpStatus.BAD_REQUEST),
  CREATE_ASK_ERROR("EU07", "매수 주문 요청 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
  CREATE_BID_ERROR("EU08", "매도 주문 요청 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
  INSUFFICIENT_FUNDS_ASK("EU09", "매수 가능 잔고가 부족합니다.", HttpStatus.BAD_REQUEST),
  INSUFFICIENT_FUNDS_BID("EU10", "매도 가능 잔고가 부족합니다.", HttpStatus.BAD_REQUEST),
  UNDER_MIN_TOTAL_ASK("EU11", "최소 매수 금액 미만입니다.", HttpStatus.BAD_REQUEST),
  UNDER_MIN_TOTAL_BID("EU12", "최소 매도 금액 미만입니다.", HttpStatus.BAD_REQUEST),
  BAD_REQUEST("EU13", "허용 되지 않은 출금 주소입니다.", HttpStatus.BAD_REQUEST),
  INVALID_PARAMETER("EU14", "잘못 된 주문 API 요청입니다.", HttpStatus.BAD_REQUEST),
  INVALID_SYMBOL("EU15", "유효하지 않은 심볼입니다.", HttpStatus.BAD_REQUEST),
  EMPTY_KEYS("EU16", "API키를 입력해주세요.", HttpStatus.BAD_REQUEST),
  API_ERROR("EU17", "API 오류입니다. 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR),
  INTERRUPTED_ERROR("EU18", "인터럽트 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
