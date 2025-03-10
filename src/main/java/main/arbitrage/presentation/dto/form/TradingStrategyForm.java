package main.arbitrage.presentation.dto.form;

import jakarta.validation.constraints.AssertTrue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.arbitrage.domain.tradingStrategy.entity.TradingStrategy;
import main.arbitrage.domain.user.entity.User;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class TradingStrategyForm {
  // Flag설정
  private Boolean lpFlag; // 청산방지 플래그
  private Boolean autoFlag; // 자동거래 플래그

  // 아래 설정은 자동거래 플래그가 false일 시 동작하지 않습니다.

  // 매매 기본 설정
  private String symbol; // 거래심볼
  private Integer leverage;
  private Integer entryCandleMinutes; // 기준 분봉 개수
  private Integer divisionCount; // 분할 횟수

  // 매매 값 설정
  private Integer kneeEntryPercent; // 구매 퍼센트 (무릎에 사서)
  private Integer shoulderEntryPercent; // 판매 퍼센트 (어깨에 팔아라)
  private Float minimumProfitTargetPercent; // 최소 이거는 먹고 팔아라
  private Float fixedProfitTargetPercent; // 난 어깨에 팔기 싫다. 무조건 해당 퍼센트만 먹겠다. 미설정시 0기입
  private Float stopLossPercent; // 손절가 (음수)
  private Float additionalBuyTargetPercent; // 추가 매수 퍼센트

  @AssertTrue(message = "청산 방지를 사용하지 않으면 자동거래를 진행할 수 없습니다.")
  public boolean isValidFlag() {
    return !(autoFlag && !lpFlag);
  }

  @AssertTrue(message = "심볼은 필수 입력값입니다.")
  public boolean isValidSymbol() {
    if (!autoFlag) return true;

    if (symbol == null) return false;

    return !symbol.trim().isEmpty();
  }

  @AssertTrue(message = "레버리지는 필수 입력값이며, 1보다 크고 10과 같거나 작아야합니다.")
  public boolean isValidLeverage() {
    if (!autoFlag) return true;

    if (leverage == null) return false;

    return leverage >= 1 && leverage <= 10;
  }

  @AssertTrue(message = "손절 퍼센트는 필수 입력값이며, 양수가 될 수 없습니다.")
  public boolean isValidStopLossPercent() {
    if (!autoFlag) return true;

    if (stopLossPercent == null) return false;

    return stopLossPercent <= 0;
  }

  @AssertTrue(message = "최소 목표 수익 퍼센트는 필수 입력값이며, 0.25%이상이어야 합니다.")
  public boolean isValidMinimumProfitTargetPercent() {
    if (!autoFlag) return true;

    if (minimumProfitTargetPercent == null) return false;

    return minimumProfitTargetPercent >= 0.25f;
  }

  @AssertTrue(message = "고정 목표 수익 퍼센트는 필수 입력값이며, 음수가 될 수 없습니다.")
  public boolean isValidFixedProfitTargetPercent() {
    if (!autoFlag) return true;

    if (fixedProfitTargetPercent == null) return false;

    return fixedProfitTargetPercent >= 0;
  }

  @AssertTrue(message = "분할 매수 횟수는 1에서 100 사이의 값이어야 합니다.")
  public boolean isValidDivisionCount() {
    if (!autoFlag) return true;

    if (divisionCount == null) return false;

    return divisionCount >= 1 && divisionCount <= 100;
  }

  @AssertTrue(message = "추가 매수 퍼센트는 필수 입력값이며, 양수가 될 수 없습니다.")
  public boolean isValidAdditionalBuyTargetPercent() {
    if (!autoFlag) return true;

    if (additionalBuyTargetPercent == null) return false;

    return additionalBuyTargetPercent <= 0;
  }

  @AssertTrue(message = "진입 캔들 분봉은 240(4시간)에서 21600(15일) 사이의 값이어야 합니다.")
  public boolean isValidEntryCandleMinutes() {
    if (!autoFlag) return true;

    if (entryCandleMinutes == null) return false;

    return entryCandleMinutes >= 240 && entryCandleMinutes <= 21600;
  }

  @AssertTrue(message = "무릎 퍼센트는 10에서 100 사이의 값이어야 합니다.")
  public boolean isValidKneeEntryPercent() {
    if (!autoFlag) return true;

    if (kneeEntryPercent == null) return false;

    return kneeEntryPercent >= 10 && kneeEntryPercent <= 100;
  }

  @AssertTrue(message = "어깨 퍼센트는 0에서 100 사이의 값이어야 합니다.")
  public boolean isValidShoulderEntryPercent() {
    if (!autoFlag) return true;

    if (shoulderEntryPercent == null) return false;

    return shoulderEntryPercent >= 0 && shoulderEntryPercent <= 100;
  }

  @AssertTrue(message = "손절 퍼센트는 추가 매수 퍼센트보다 작아야 합니다")
  public boolean isValidStopLossAndAdditionalBuyTarget() {
    if (!autoFlag) return true;

    if (stopLossPercent == null || additionalBuyTargetPercent == null) return false;

    return stopLossPercent < additionalBuyTargetPercent;
  }

  @AssertTrue(message = "최소 목표 수익 퍼센트는 고정 목표 수익 퍼센트보다 클 수 없습니다.")
  public boolean isValidProfitTargets() {
    if (!autoFlag) return true;

    if (fixedProfitTargetPercent == null || minimumProfitTargetPercent == null) return false;

    return fixedProfitTargetPercent == 0
        || (fixedProfitTargetPercent > 0 && minimumProfitTargetPercent <= fixedProfitTargetPercent);
  }

  @AssertTrue(message = "어깨 퍼센트는 무릎 퍼센트보다 작을 수 없습니다.")
  public boolean isValidEntryPercents() {
    if (!autoFlag) return true;

    if (shoulderEntryPercent == null || kneeEntryPercent == null) return false;

    return shoulderEntryPercent >= kneeEntryPercent;
  }

  public static TradingStrategyForm fromEntity(User user, TradingStrategy tradingStrategy) {
    return TradingStrategyForm.builder()
        .lpFlag(user.isLpFlag())
        .autoFlag(user.isAutoFlag())
        .symbol(tradingStrategy.getSymbol().getName())
        .leverage(tradingStrategy.getLeverage())
        .stopLossPercent(tradingStrategy.getStopLossPercent())
        .minimumProfitTargetPercent(tradingStrategy.getMinimumProfitTargetPercent())
        .fixedProfitTargetPercent(tradingStrategy.getFixedProfitTargetPercent())
        .divisionCount(tradingStrategy.getDivisionCount())
        .additionalBuyTargetPercent(tradingStrategy.getAdditionalBuyTargetPercent())
        .entryCandleMinutes(tradingStrategy.getEntryCandleMinutes())
        .kneeEntryPercent(tradingStrategy.getKneeEntryPercent())
        .shoulderEntryPercent(tradingStrategy.getShoulderEntryPercent())
        .build();
  }

  public static TradingStrategyForm fromEntity(User user) {
    return TradingStrategyForm.builder()
        .lpFlag(user.isLpFlag())
        .autoFlag(user.isAutoFlag())
        .build();
  }
}
