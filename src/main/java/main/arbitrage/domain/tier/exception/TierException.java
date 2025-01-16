package main.arbitrage.domain.tier.exception;

import main.arbitrage.global.exception.common.BaseException;

public class TierException extends BaseException {

  public TierException(TierErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public TierException(TierErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public TierException(TierErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public TierException(TierErrorCode errorCode) {
    super(errorCode);
  }
}
