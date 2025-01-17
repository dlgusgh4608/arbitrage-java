package main.arbitrage.domain.autoTradingStrategy.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.autoTradingStrategy.entity.AutoTradingStrategy;
import main.arbitrage.domain.autoTradingStrategy.exception.AutoTradingStrategyErrorCode;
import main.arbitrage.domain.autoTradingStrategy.exception.AutoTradingStrategyException;
import main.arbitrage.domain.autoTradingStrategy.repository.AutoTradingStrategyRepository;
import main.arbitrage.domain.symbol.entity.Symbol;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutoTradingStrategyService {
  private final AutoTradingStrategyRepository autoTradingStrategyRepository;

  public AutoTradingStrategy create(
      Long userId,
      Symbol symbol,
      float stopLossPercent,
      float minimumProfitTargetPercent,
      float fixedProfitTargetPercent,
      int divisionCount,
      float additionalBuyTargetPercent,
      int entryCandleMinutes,
      int kneeEntryPercent,
      int shoulderEntryPercent) {
    try {
      return autoTradingStrategyRepository.save(
          AutoTradingStrategy.builder()
              .userId(userId)
              .symbol(symbol)
              .stopLossPercent(stopLossPercent)
              .minimumProfitTargetPercent(minimumProfitTargetPercent)
              .fixedProfitTargetPercent(fixedProfitTargetPercent)
              .divisionCount(divisionCount)
              .additionalBuyTargetPercent(additionalBuyTargetPercent)
              .entryCandleMinutes(entryCandleMinutes)
              .kneeEntryPercent(kneeEntryPercent)
              .shoulderEntryPercent(shoulderEntryPercent)
              .build());
    } catch (AutoTradingStrategyException e) {
      throw e;
    } catch (Exception e) {
      throw new AutoTradingStrategyException(AutoTradingStrategyErrorCode.UNKNOWN, e);
    }
  }

  public Optional<AutoTradingStrategy> findById(Long userId) {
    try {
      return autoTradingStrategyRepository.findById(userId);
    } catch (AutoTradingStrategyException e) {
      throw e;
    } catch (Exception e) {
      throw new AutoTradingStrategyException(AutoTradingStrategyErrorCode.UNKNOWN, e);
    }
  }
}
