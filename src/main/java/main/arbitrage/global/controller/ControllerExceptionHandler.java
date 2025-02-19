package main.arbitrage.global.controller;

import lombok.extern.slf4j.Slf4j;
import main.arbitrage.auth.exception.AuthException;
import main.arbitrage.domain.buyOrder.exception.BuyOrderException;
import main.arbitrage.domain.exchangeRate.exception.ExchangeRateException;
import main.arbitrage.domain.grade.exception.GradeException;
import main.arbitrage.domain.oauthUser.exception.OAuthUserException;
import main.arbitrage.domain.price.exception.PriceException;
import main.arbitrage.domain.sellOrder.exception.SellOrderException;
import main.arbitrage.domain.symbol.exception.SymbolException;
import main.arbitrage.domain.tier.exception.TierException;
import main.arbitrage.domain.tradingStrategy.exception.TradingStrategyException;
import main.arbitrage.domain.user.exception.UserException;
import main.arbitrage.domain.userEnv.exception.UserEnvException;
import main.arbitrage.global.exception.ErrorResponse;
import main.arbitrage.global.exception.common.BaseException;
import main.arbitrage.global.util.aes.exception.CryptoException;
import main.arbitrage.infrastructure.binance.exception.BinanceException;
import main.arbitrage.infrastructure.email.exception.SendMailException;
import main.arbitrage.infrastructure.oauthValidator.exception.OauthValidatorException;
import main.arbitrage.infrastructure.upbit.exception.UpbitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {
  @ExceptionHandler({
    SymbolException.class,
    UserException.class,
    UserEnvException.class,
    CryptoException.class,
    SendMailException.class,
    BinanceException.class,
    UpbitException.class,
    OauthValidatorException.class,
    PriceException.class,
    OAuthUserException.class,
    BuyOrderException.class,
    SellOrderException.class,
    ExchangeRateException.class,
    TradingStrategyException.class,
    GradeException.class,
    TierException.class,
    AuthException.class
  })
  public ResponseEntity<ErrorResponse> handleCustomExceptions(BaseException e) {
    String logTitle = e.getClass().getSimpleName().replace("Exception", "Error");
    log.error("({}):\t{} - {}", logTitle, e.getMessage(), e.getServerMessage(), e);
    return createResponse(e);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleRestException(Exception e) {
    log.error("INTERNAL_SERVER_ERROR", e);
    return createResponse();
  }

  private ResponseEntity<ErrorResponse> createResponse(BaseException e) {
    return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(ErrorResponse.of(e));
  }

  private ResponseEntity<ErrorResponse> createResponse() {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.builder().code("G01").message("알 수 없는 에러입니다.").build());
  }
}
