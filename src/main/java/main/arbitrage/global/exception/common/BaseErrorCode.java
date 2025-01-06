package main.arbitrage.global.exception.common;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
  String getCode();

  String getClientMessage();

  HttpStatus getHttpStatus();
}
