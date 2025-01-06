package main.arbitrage.global.exception;

import lombok.extern.slf4j.Slf4j;
import main.arbitrage.auth.exception.AuthException;
import main.arbitrage.domain.symbol.exception.SymbolException;
import main.arbitrage.domain.user.exception.UserException;
import main.arbitrage.domain.userEnv.exception.UserEnvException;
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
    AuthException.class,
    GlobalException.class
  })
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(BaseException e) {
    String logTitle = e.getClass().getSimpleName().replace("Exception", "Error");
    errorLog(logTitle, e);
    return createResponse(e);
  }

  private void errorLog(String logTitle, BaseException e) {
    String code = e.getErrorCode().getCode();
    String clientMessage = e.getErrorCode().getClientMessage();
    String serverMessage = e.getServerMessage();

    log.error("[{}({})]: {}\n{}", logTitle, code, clientMessage, serverMessage);
  }

  private ResponseEntity<ErrorResponse> createResponse(BaseException e) {
    return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(ErrorResponse.of(e));
  }
}
