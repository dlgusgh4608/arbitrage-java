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
import main.arbitrage.infrastructure.email.exception.SendMailException;
import main.arbitrage.infrastructure.exchange.binance.exception.BinanceException;
import main.arbitrage.infrastructure.exchange.upbit.exception.UpbitException;
import main.arbitrage.infrastructure.oauthValidator.exception.OauthValidatorException;
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
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(BaseException e) {
    String logTitle = e.getClass().getSimpleName().replace("Exception", "Error");
    log.error("({}):\t{}", logTitle, e.getMessage(), e);
    return createResponse(e);
  }

  private ResponseEntity<ErrorResponse> createResponse(BaseException e) {
    return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(ErrorResponse.of(e));
  }
}
