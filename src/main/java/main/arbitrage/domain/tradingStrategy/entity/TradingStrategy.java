package main.arbitrage.domain.tradingStrategy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.tradingStrategy.exception.TradingStrategyErrorCode;
import main.arbitrage.domain.tradingStrategy.exception.TradingStrategyException;
import main.arbitrage.domain.user.entity.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "trading_strategy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradingStrategy {
  @Id
  @Column(name = "user_id")
  private Long userId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "symbol_id", nullable = false)
  private Symbol symbol;

  @Column(name = "leverage", nullable = false, columnDefinition = "SMALLINT")
  private int leverage;

  /*
   * 손절 퍼센트
   *
   * 1. 양수는 들어갈 수 없음
   * 2. 추가 매수 퍼센트(additionalBuyTargetPercent) 보다 작을 수 없음
   * ex)
   *  값이 -1.5이면 수익율이 -1.5%에 도달했을때
   *  주문을 시도
   */
  @Column(name = "stop_loss_percent", nullable = false, columnDefinition = "REAL")
  private float stopLossPercent;

  /*
   * 최소 목표 수익 퍼센트
   *
   * 1. 음수는 들어갈 수 없음
   * 2. 최소값 0.25
   * 3. 최소 목표 수익 퍼센트는 고정 목표 수익 퍼센트 보다 클 수 없음.
   *  upbit 거래 수수료 0.05%, binance 거래 수수료 taker 0.05%, maker 0.02%
   *  거래시 (upbit 0.1% + binance 최대 0.1%)총 0.2% + upbit 시장가에 대한 0.05%의 차이 가능성 = 0.25%
   */
  @Column(name = "minimum_profit_target_percent", nullable = false, columnDefinition = "REAL")
  private float minimumProfitTargetPercent;

  /*
   * 고정 목표 수익 퍼센트
   *
   * 1. 음수는 들어갈 수 없음
   * 2. 최소 목표 수익 퍼센트보다 작을 수 없음
   *  upbit 거래 수수료 0.05%, binance 거래 수수료 taker 0.05%, maker 0.02%
   *  거래시 (upbit 0.1% + binance 최대 0.1%)총 0.2% + upbit 시장가에 대한 0.05%의 차이 가능성 = 0.25%
   * 3. 해당 값이 존재시 shoulder_entry_percent의 값을 무시하고 해당 퍼센트에 도달하면 매도
   * 4. 해당 값을 사용하고 싶지 않을 시 0을 저장
   * ex)
   *  값이 1.5이면 수익율이 1.5%에 도달했을때
   *  주문을 시도
   */
  @Column(name = "fixed_profit_target_percent", nullable = false, columnDefinition = "REAL")
  private float fixedProfitTargetPercent;

  /*
   * 분할 매수 횟수
   *
   * 1. 최소값 1
   * 2. 최대값 100
   * ex)
   *  분할 매수 횟수가 5이고
   *  지갑 잔액(진행중인 거래가 없을 시) krw 1,000,000, usdt 740이면
   *  200,000krw, 148usdt에 대한 코인을 매수
   */
  @Column(name = "division_count", nullable = false, columnDefinition = "SMALLINT")
  private int divisionCount;

  /*
   * 추가 매수 퍼센트
   *
   * 1. 양수는 들어갈 수 없음.
   * 2. 불타기 없음, 물타기만 있음
   * 3. 손절 퍼센트(stopLossPercent보다 클 수 없음)
   */
  @Column(name = "additional_buy_target_percent", nullable = false, columnDefinition = "REAL")
  private float additionalBuyTargetPercent;

  /*
   * 최초 매수 기준가를 정하는 분봉
   *
   * 1. 음수는 들어갈 수 없음.
   * 2. 240보다 작을 수 없음 (4시간)
   * 3. 해당 값의 1/2에 해당하는 시간마다 재할당
   * ex)
   *  값이 240이면 240분(4시간)에 대한
   *  무릎가격(25%)에 도달하면 매수 시도
   */
  @Column(name = "entry_candle_minutes", nullable = false, columnDefinition = "SMALLINT")
  private int entryCandleMinutes;

  /*
   * 무릎 퍼센트
   * 1. 음수는 들어갈 수 없음
   * 2. 최소 10% (이보다 낮으면 매수가 잘 안될듯...?)
   * 3. entry_candle_minutes의 값을 통해 가져온 이전 추세에 대한 매수 퍼센트
   * ex)
   *  값이 10%이고 최소값 0%, 최대값1%라 할때
   *  0.1%에 도달했을때 매수 시도
   */
  @Column(name = "knee_entry_percent", nullable = false, columnDefinition = "SMALLINT")
  private int kneeEntryPercent;

  /*
   * 어깨 퍼센트
   * 1. 음수는 들어갈 수 없음
   * 2. 무릎 퍼센트 보다 작을 수 없음
   * 3. entry_candle_minutes의 값을 통해 가져온 이전 추세에 대한 매도 퍼센트
   * ex)
   *  값이 80%이고 최소값 0%, 최대값1%라 할때
   *  0.8%에 도달했을때 minimumProfitTargetPercent(최소 수익율)이 넘으면 매도 시도
   *
   * 위의 kneeEntryPercent의 예시와 같은 상황에 매수가 되었다하면
   * 0.8% - 0.1% - 0.2%(최대 수수료) = 0.5%의 수익율을 예상
   */
  @Column(name = "shoulder_entry_percent", nullable = false, columnDefinition = "SMALLINT")
  private int shoulderEntryPercent;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP(6) WITH TIME ZONE")
  private Timestamp createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP(6) WITH TIME ZONE")
  private Timestamp updatedAt;

  @Builder
  public TradingStrategy(
      User user,
      Symbol symbol,
      int leverage,
      float stopLossPercent,
      float minimumProfitTargetPercent,
      float fixedProfitTargetPercent,
      int divisionCount,
      float additionalBuyTargetPercent,
      int entryCandleMinutes,
      int kneeEntryPercent,
      int shoulderEntryPercent) {

    validateAll(
        stopLossPercent,
        leverage,
        minimumProfitTargetPercent,
        fixedProfitTargetPercent,
        divisionCount,
        additionalBuyTargetPercent,
        entryCandleMinutes,
        kneeEntryPercent,
        shoulderEntryPercent);

    this.user = user;
    this.symbol = symbol;
    this.leverage = leverage;
    this.stopLossPercent = stopLossPercent;
    this.minimumProfitTargetPercent = minimumProfitTargetPercent;
    this.fixedProfitTargetPercent = fixedProfitTargetPercent;
    this.divisionCount = divisionCount;
    this.additionalBuyTargetPercent = additionalBuyTargetPercent;
    this.entryCandleMinutes = entryCandleMinutes;
    this.kneeEntryPercent = kneeEntryPercent;
    this.shoulderEntryPercent = shoulderEntryPercent;
  }

  private void validateAll(
      float stopLossPercent,
      int leverage,
      float minimumProfitTargetPercent,
      float fixedProfitTargetPercent,
      int divisionCount,
      float additionalBuyTargetPercent,
      int entryCandleMinutes,
      int kneeEntryPercent,
      int shoulderEntryPercent) {
    validateLeverage(leverage);
    validateStopLossPercent(stopLossPercent, additionalBuyTargetPercent);
    validateMinimumProfitTargetPercent(minimumProfitTargetPercent, fixedProfitTargetPercent);
    validateFixedProfitTargetPercent(fixedProfitTargetPercent, minimumProfitTargetPercent);
    validateDivisionCount(divisionCount);
    validateAdditionalBuyTargetPercent(additionalBuyTargetPercent, stopLossPercent);
    validateEntryCandleMinutes(entryCandleMinutes);
    validateKneeEntryPercent(kneeEntryPercent);
    validateShoulderEntryPercent(shoulderEntryPercent, kneeEntryPercent);
  }

  private void validateLeverage(int leverage) {
    if (leverage < 1) {
      throw new TradingStrategyException(TradingStrategyErrorCode.LEVERAGE_TOO_LOW);
    }

    if (leverage > 10) {
      throw new TradingStrategyException(TradingStrategyErrorCode.LEVERAGE_TOO_HIGH);
    }
  }

  private void validateStopLossPercent(float stopLossPercent, float additionalBuyTargetPercent) {
    if (stopLossPercent >= 0) {
      throw new TradingStrategyException(TradingStrategyErrorCode.STOP_LOSS_PERCENT_POSITIVE);
    }
    if (stopLossPercent > additionalBuyTargetPercent) {
      throw new TradingStrategyException(
          TradingStrategyErrorCode.STOP_LOSS_PERCENT_GREATER_THAN_ADDITIONAL);
    }
  }

  private void validateMinimumProfitTargetPercent(
      float minimumProfitTargetPercent, float fixedProfitTargetPercent) {
    if (minimumProfitTargetPercent < 0.25f) {
      throw new TradingStrategyException(TradingStrategyErrorCode.MINIMUM_PROFIT_TOO_LOW);
    }
    if (minimumProfitTargetPercent > fixedProfitTargetPercent && fixedProfitTargetPercent != 0) {
      throw new TradingStrategyException(
          TradingStrategyErrorCode.MINIMUM_PROFIT_GREATER_THAN_FIXED);
    }
  }

  private void validateFixedProfitTargetPercent(
      float fixedProfitTargetPercent, float minimumProfitTargetPercent) {
    if (fixedProfitTargetPercent < 0) {
      throw new TradingStrategyException(TradingStrategyErrorCode.FIXED_PROFIT_NEGATIVE);
    }
    if (fixedProfitTargetPercent != 0 && fixedProfitTargetPercent < minimumProfitTargetPercent) {
      throw new TradingStrategyException(TradingStrategyErrorCode.FIXED_PROFIT_LESS_THAN_MINIMUM);
    }
  }

  private void validateDivisionCount(int divisionCount) {
    if (divisionCount < 1) {
      throw new TradingStrategyException(TradingStrategyErrorCode.DIVISION_COUNT_TOO_LOW);
    }
    if (divisionCount > 100) {
      throw new TradingStrategyException(TradingStrategyErrorCode.DIVISION_COUNT_TOO_HIGH);
    }
  }

  private void validateAdditionalBuyTargetPercent(
      float additionalBuyTargetPercent, float stopLossPercent) {
    if (additionalBuyTargetPercent >= 0) {
      throw new TradingStrategyException(TradingStrategyErrorCode.ADDITIONAL_BUY_PERCENT_POSITIVE);
    }
    if (additionalBuyTargetPercent < stopLossPercent) {
      throw new TradingStrategyException(
          TradingStrategyErrorCode.ADDITIONAL_BUY_PERCENT_LESS_THAN_STOP_LOSS);
    }
  }

  private void validateEntryCandleMinutes(int entryCandleMinutes) {
    if (entryCandleMinutes < 240) {
      throw new TradingStrategyException(TradingStrategyErrorCode.ENTRY_CANDLE_MINUTES_TOO_LOW);
    }

    if (entryCandleMinutes > 21600) {
      throw new TradingStrategyException(TradingStrategyErrorCode.ENTRY_CANDLE_MINUTES_TOO_HIGH);
    }
  }

  private void validateKneeEntryPercent(int kneeEntryPercent) {
    if (kneeEntryPercent < 10) {
      throw new TradingStrategyException(TradingStrategyErrorCode.KNEE_ENTRY_PERCENT_TOO_LOW);
    }
    if (kneeEntryPercent > 100) {
      throw new TradingStrategyException(TradingStrategyErrorCode.KNEE_ENTRY_PERCENT_TOO_HIGH);
    }
  }

  private void validateShoulderEntryPercent(int shoulderEntryPercent, int kneeEntryPercent) {
    if (shoulderEntryPercent < 0) {
      throw new TradingStrategyException(TradingStrategyErrorCode.SHOULDER_ENTRY_PERCENT_NEGATIVE);
    }
    if (shoulderEntryPercent < kneeEntryPercent) {
      throw new TradingStrategyException(
          TradingStrategyErrorCode.SHOULDER_ENTRY_PERCENT_LESS_THAN_KNEE);
    }
    if (shoulderEntryPercent > 100) {
      throw new TradingStrategyException(TradingStrategyErrorCode.SHOULDER_ENTRY_PERCENT_TOO_HIGH);
    }
  }

  public void update(
      Symbol symbol,
      int leverage,
      float stopLossPercent,
      float minimumProfitTargetPercent,
      float fixedProfitTargetPercent,
      int divisionCount,
      float additionalBuyTargetPercent,
      int entryCandleMinutes,
      int kneeEntryPercent,
      int shoulderEntryPercent) {
    validateAll(
        stopLossPercent,
        leverage,
        minimumProfitTargetPercent,
        fixedProfitTargetPercent,
        divisionCount,
        additionalBuyTargetPercent,
        entryCandleMinutes,
        kneeEntryPercent,
        shoulderEntryPercent);

    this.symbol = symbol;
    this.stopLossPercent = stopLossPercent;
    this.minimumProfitTargetPercent = minimumProfitTargetPercent;
    this.fixedProfitTargetPercent = fixedProfitTargetPercent;
    this.divisionCount = divisionCount;
    this.additionalBuyTargetPercent = additionalBuyTargetPercent;
    this.entryCandleMinutes = entryCandleMinutes;
    this.kneeEntryPercent = kneeEntryPercent;
    this.shoulderEntryPercent = shoulderEntryPercent;
  }
}
