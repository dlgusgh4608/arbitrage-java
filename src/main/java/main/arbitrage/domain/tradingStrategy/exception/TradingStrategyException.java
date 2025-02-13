package main.arbitrage.domain.tradingStrategy.exception;

import main.arbitrage.global.exception.common.BaseException;

public class TradingStrategyException extends BaseException {

  public TradingStrategyException(
      TradingStrategyErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public TradingStrategyException(TradingStrategyErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public TradingStrategyException(TradingStrategyErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public TradingStrategyException(TradingStrategyErrorCode errorCode) {
    super(errorCode);
  }
}
