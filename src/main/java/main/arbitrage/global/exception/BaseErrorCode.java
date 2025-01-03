package main.arbitrage.global.exception;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
  String getCode();

  String getClientMessage();

  HttpStatus getHttpStatus();
}
