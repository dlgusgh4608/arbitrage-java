package main.arbitrage.infrastructure.crawler.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CrawlerErrorCode implements BaseErrorCode {
  ZERO_BYTE("EX01", "구글 크롤링 리턴값이 0bytes 입니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
