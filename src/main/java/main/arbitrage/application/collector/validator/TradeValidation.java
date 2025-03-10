package main.arbitrage.application.collector.validator;

import main.arbitrage.infrastructure.exchanges.dto.TradeDto;
import org.springframework.stereotype.Component;

@Component
public class TradeValidation {
  private static final long MAX_TIME_DIFFERENCE = 30000;

  public boolean isValidTradePair(TradeDto upbit, TradeDto binance) {
    if (upbit == null || binance == null) return false;

    return Math.abs(upbit.getTimestamp() - binance.getTimestamp()) <= MAX_TIME_DIFFERENCE;
  }
}
