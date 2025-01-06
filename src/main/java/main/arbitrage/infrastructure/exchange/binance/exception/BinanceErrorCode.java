package main.arbitrage.infrastructure.exchange.binance.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BinanceErrorCode implements BaseErrorCode {
  UNKNOWN("EB01", "알 수 없는 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  DISCONNECTED("EB02", "내부 오류가 발생했습니다. 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR),
  UNAUTHORIZED("EB03", "이 요청을 실행할 권한이 없습니다.", HttpStatus.UNAUTHORIZED),
  TOO_MANY_REQUESTS("EB04", "너무 많은 요청이 대기 중입니다.", HttpStatus.SERVICE_UNAVAILABLE),
  INVALID_TIMESTAMP("EB05", "타임스탬프가 recvWindow를 벗어났습니다.", HttpStatus.BAD_REQUEST),
  INVALID_SIGNATURE("EB06", "이 요청에 대한 서명이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
  MANDATORY_PARAM_EMPTY_OR_MALFORMED(
      "EB07", "필수 파라미터가 전송되지 않았거나, 비어있거나, 잘못되었습니다.", HttpStatus.BAD_REQUEST),
  BAD_SYMBOL("EB08", "잘못된 심볼입니다.", HttpStatus.BAD_REQUEST),
  NEW_ORDER_REJECTED("EB09", "새로운 주문이 거부되었습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  NO_SUCH_ORDER("EB10", "주문이 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
  BAD_API_KEY_FMT("EB11", "유효하지 않은 API 키 입니다.", HttpStatus.BAD_REQUEST),
  INVALID_API_KEY_IP_PERMISSION("EB12", "API 키, IP 또는 권한이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
  BALANCE_NOT_SUFFICIENT("EB13", "잔액이 부족합니다.", HttpStatus.BAD_REQUEST),
  PRICE_LESS_THAN_ZERO("EB14", "가격이 0보다 작습니다.", HttpStatus.BAD_REQUEST),
  QTY_LESS_THAN_ZERO("EB15", "수량이 0보다 작습니다.", HttpStatus.BAD_REQUEST),
  AMOUNT_MUST_BE_POSITIVE("EB16", "금액은 양수여야 합니다.", HttpStatus.BAD_REQUEST),
  EMPTY_KEYS("EB17", "API키를 입력해주세요.", HttpStatus.BAD_REQUEST),
  API_ERROR("EB18", "API 오류입니다. 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR),
  BAD_PARAMS("EB19", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
