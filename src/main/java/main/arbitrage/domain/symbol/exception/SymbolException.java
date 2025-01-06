package main.arbitrage.domain.symbol.exception;

import main.arbitrage.global.exception.common.BaseException;

public class SymbolException extends BaseException {

  public SymbolException(SymbolErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public SymbolException(SymbolErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public SymbolException(SymbolErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public SymbolException(SymbolErrorCode errorCode) {
    super(errorCode);
  }
}
