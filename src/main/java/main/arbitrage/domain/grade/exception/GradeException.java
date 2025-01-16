package main.arbitrage.domain.grade.exception;

import main.arbitrage.global.exception.common.BaseException;

public class GradeException extends BaseException {

  public GradeException(GradeErrorCode errorCode, String serverMessage, Throwable cause) {
    super(errorCode, serverMessage, cause);
  }

  public GradeException(GradeErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public GradeException(GradeErrorCode errorCode, String serverMessage) {
    super(errorCode, serverMessage);
  }

  public GradeException(GradeErrorCode errorCode) {
    super(errorCode);
  }
}
