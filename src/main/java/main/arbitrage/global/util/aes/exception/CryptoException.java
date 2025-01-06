package main.arbitrage.global.util.aes.exception;

import main.arbitrage.global.exception.common.BaseException;

public class CryptoException extends BaseException {

  public CryptoException(CryptoErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public CryptoException(CryptoErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public CryptoException(CryptoErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public CryptoException(CryptoErrorCode errorCode) {
    super(errorCode);
  }
}
