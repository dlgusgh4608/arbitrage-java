package main.arbitrage.infrastructure.crawler.exception;

import main.arbitrage.global.exception.BaseException;

public class CrawlerException extends BaseException {

  public CrawlerException(CrawlerErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public CrawlerException(CrawlerErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public CrawlerException(CrawlerErrorCode errorCode) {
    super(errorCode);
  }
}