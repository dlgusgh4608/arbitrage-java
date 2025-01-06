package main.arbitrage.infrastructure.crawler.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CrawlerErrorCode implements BaseErrorCode {
  UNKNOWN("CRW01", "알 수 없는 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  ZERO_BYTE("CRW02", "리턴값이 0bytes 입니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
