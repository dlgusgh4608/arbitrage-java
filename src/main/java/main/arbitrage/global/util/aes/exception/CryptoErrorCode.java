package main.arbitrage.global.util.aes.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CryptoErrorCode implements BaseErrorCode {
  INVALID_KEY("CRT01", "유효하지 않은 암호화 키입니다", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_ALGORITHM("CRT02", "지원하지 않는 암호화 알고리즘입니다", HttpStatus.INTERNAL_SERVER_ERROR),
  ENCRYPTION_FAILED("CRT03", "암호화 처리 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
  DECRYPTION_FAILED("CRT04", "복호화 처리 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_PADDING("CRT05", "잘못된 패딩 형식입니다", HttpStatus.BAD_REQUEST),
  INVALID_IV("CRT06", "유효하지 않은 초기화 벡터(IV)입니다", HttpStatus.BAD_REQUEST),
  INVALID_INPUT("CRT07", "유효하지 않은 입력값입니다", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
