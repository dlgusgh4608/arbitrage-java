package main.arbitrage.domain.autoTradingStrategy.exception;

import main.arbitrage.global.exception.common.BaseException;

public class AutoTradingStrategyException extends BaseException {

  public AutoTradingStrategyException(
      AutoTradingStrategyErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public AutoTradingStrategyException(AutoTradingStrategyErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public AutoTradingStrategyException(
      AutoTradingStrategyErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public AutoTradingStrategyException(AutoTradingStrategyErrorCode errorCode) {
    super(errorCode);
  }
}
