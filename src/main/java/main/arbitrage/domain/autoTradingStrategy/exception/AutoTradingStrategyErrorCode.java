package main.arbitrage.domain.autoTradingStrategy.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.global.exception.common.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AutoTradingStrategyErrorCode implements BaseErrorCode {
  UNKNOWN("TS01", "알 수 없는 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  // 손절 퍼센트 관련 에러
  STOP_LOSS_PERCENT_POSITIVE("TS02", "손절 퍼센트는 음수여야 합니다.", HttpStatus.BAD_REQUEST),
  STOP_LOSS_PERCENT_GREATER_THAN_ADDITIONAL(
      "TS03", "손절 퍼센트는 추가 매수 퍼센트보다 작아야 합니다.", HttpStatus.BAD_REQUEST),

  // 최소 목표 수익 퍼센트 관련 에러
  MINIMUM_PROFIT_TOO_LOW("TS04", "최소 목표 수익 퍼센트는 0.25% 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
  MINIMUM_PROFIT_GREATER_THAN_FIXED(
      "TS05", "최소 목표 수익 퍼센트는 고정 목표 수익 퍼센트보다 클 수 없습니다.", HttpStatus.BAD_REQUEST),

  // 고정 목표 수익 퍼센트 관련 에러
  FIXED_PROFIT_NEGATIVE("TS06", "고정 목표 수익 퍼센트는 음수일 수 없습니다.", HttpStatus.BAD_REQUEST),
  FIXED_PROFIT_LESS_THAN_MINIMUM(
      "TS07", "고정 목표 수익 퍼센트는 최소 목표 수익 퍼센트보다 작을 수 없습니다.", HttpStatus.BAD_REQUEST),

  // 분할 매수 횟수 관련 에러
  DIVISION_COUNT_TOO_LOW("TS08", "분할 매수 횟수는 1 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
  DIVISION_COUNT_TOO_HIGH("TS09", "분할 매수 횟수는 100 이하여야 합니다.", HttpStatus.BAD_REQUEST),

  // 추가 매수 퍼센트 관련 에러
  ADDITIONAL_BUY_PERCENT_POSITIVE("TS10", "추가 매수 퍼센트는 음수여야 합니다.", HttpStatus.BAD_REQUEST),
  ADDITIONAL_BUY_PERCENT_LESS_THAN_STOP_LOSS(
      "TS11", "추가 매수 퍼센트는 손절 퍼센트보다 커야 합니다.", HttpStatus.BAD_REQUEST),

  // 매수 기준 분봉 관련 에러
  ENTRY_CANDLE_MINUTES_TOO_LOW("TS12", "매수 기준 분봉은 240분(4시간) 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
  ENTRY_CANDLE_MINUTES_TOO_HIGH("TS13", "매수 기준 분봉은 21600분(15일) 이하여야 합니다.", HttpStatus.BAD_REQUEST),

  // 무릎 퍼센트 관련 에러
  KNEE_ENTRY_PERCENT_TOO_LOW("TS14", "무릎 퍼센트는 10% 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
  KNEE_ENTRY_PERCENT_TOO_HIGH("TS15", "무릎 퍼센트는 100% 이하여야 합니다.", HttpStatus.BAD_REQUEST),

  // 어깨 퍼센트 관련 에러
  SHOULDER_ENTRY_PERCENT_NEGATIVE("TS16", "어깨 퍼센트는 음수일 수 없습니다.", HttpStatus.BAD_REQUEST),
  SHOULDER_ENTRY_PERCENT_LESS_THAN_KNEE(
      "TS17", "어깨 퍼센트는 무릎 퍼센트보다 작을 수 없습니다.", HttpStatus.BAD_REQUEST),
  SHOULDER_ENTRY_PERCENT_TOO_HIGH("TS18", "어깨 퍼센트는 100% 이하여야 합니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String clientMessage;
  private final HttpStatus httpStatus;
}
