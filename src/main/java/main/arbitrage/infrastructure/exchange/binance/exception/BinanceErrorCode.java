package main.arbitrage.infrastructure.exchange.binance.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BinanceErrorCode implements BaseErrorCode {
  UNKNOWN("BPR01", "(바이낸스) 알 수 없는 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  DISCONNECTED("BPR01", "(바이낸스) 내부 오류가 발생했습니다. 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR),
  UNAUTHORIZED("BPR01", "(바이낸스) 이 요청을 실행할 권한이 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  TOO_MANY_REQUESTS("BPR01", "(바이낸스) 너무 많은 요청이 대기 중입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_TIMESTAMP("BPR01", "(바이낸스) 타임스탬프가 recvWindow를 벗어났습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_SIGNATURE("BPR01", "(바이낸스) 이 요청에 대한 서명이 유효하지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  MANDATORY_PARAM_EMPTY_OR_MALFORMED(
      "BPR01", "(바이낸스) 필수 파라미터가 전송되지 않았거나, 비어있거나, 잘못되었습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  BAD_SYMBOL("BPR01", "(바이낸스) 잘못된 심볼입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  NEW_ORDER_REJECTED("BPR01", "(바이낸스) 새로운 주문이 거부되었습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  NO_SUCH_ORDER("BPR01", "(바이낸스) 주문이 존재하지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  BAD_API_KEY_FMT("BPR01", "(바이낸스) 유효하지 않은 API 키 입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_API_KEY_IP_PERMISSION(
      "BPR01", "(바이낸스) API 키, IP 또는 권한이 유효하지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  BALANCE_NOT_SUFFICIENT("BPR01", "(바이낸스) 잔액이 부족합니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  PRICE_LESS_THAN_ZERO("BPR01", "(바이낸스) 가격이 0보다 작습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  QTY_LESS_THAN_ZERO("BPR01", "(바이낸스) 수량이 0보다 작습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  AMOUNT_MUST_BE_POSITIVE("BPR01", "(바이낸스) 금액은 양수여야 합니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  UNKNOWN_ERROR("BPR01", "(바이낸스) 알 수 없는 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
