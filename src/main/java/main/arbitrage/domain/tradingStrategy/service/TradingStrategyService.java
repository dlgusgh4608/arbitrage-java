package main.arbitrage.domain.tradingStrategy.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.tradingStrategy.entity.TradingStrategy;
import main.arbitrage.domain.tradingStrategy.exception.TradingStrategyErrorCode;
import main.arbitrage.domain.tradingStrategy.exception.TradingStrategyException;
import main.arbitrage.domain.tradingStrategy.repository.TradingStrategyRepository;
import main.arbitrage.domain.user.entity.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradingStrategyService {
  private final TradingStrategyRepository autoTradingStrategyRepository;

  public TradingStrategy create(
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
    try {
      return autoTradingStrategyRepository.save(
          TradingStrategy.builder()
              .user(user)
              .symbol(symbol)
              .leverage(leverage)
              .stopLossPercent(stopLossPercent)
              .minimumProfitTargetPercent(minimumProfitTargetPercent)
              .fixedProfitTargetPercent(fixedProfitTargetPercent)
              .divisionCount(divisionCount)
              .additionalBuyTargetPercent(additionalBuyTargetPercent)
              .entryCandleMinutes(entryCandleMinutes)
              .kneeEntryPercent(kneeEntryPercent)
              .shoulderEntryPercent(shoulderEntryPercent)
              .build());
    } catch (TradingStrategyException e) {
      throw e;
    } catch (Exception e) {
      throw new TradingStrategyException(TradingStrategyErrorCode.UNKNOWN, e);
    }
  }

  public Optional<TradingStrategy> findByUserId(Long userId) {
    try {
      return autoTradingStrategyRepository.findById(userId);
    } catch (TradingStrategyException e) {
      throw e;
    } catch (Exception e) {
      throw new TradingStrategyException(TradingStrategyErrorCode.UNKNOWN, e);
    }
  }
}
