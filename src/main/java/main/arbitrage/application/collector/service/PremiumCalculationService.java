package main.arbitrage.application.collector.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.global.util.math.MathUtil;
import main.arbitrage.infrastructure.event.dto.PremiumDto;
import main.arbitrage.application.collector.dto.TradeDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PremiumCalculationService {
    public PremiumDto calculatePremium(
            TradeDto upbit,
            TradeDto binance,
            double usdToKrw,
            String symbol
    ) {

        double premium = MathUtil.calculatePremium(upbit.getPrice(), binance.getPrice(), usdToKrw);

        return PremiumDto.builder()
                .symbol(symbol)
                .premium(premium)
                .upbit(upbit.getPrice())
                .binance(binance.getPrice())
                .usdToKrw(usdToKrw)
                .upbitTradeAt(upbit.getTimestamp())
                .binanceTradeAt(binance.getTimestamp())
                .build();
    }
}