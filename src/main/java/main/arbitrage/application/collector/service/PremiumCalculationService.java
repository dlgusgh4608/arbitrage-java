package main.arbitrage.application.collector.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.global.util.calculator.FinancialCalculator;
import main.arbitrage.global.util.currency.CurrencyConverter;
import main.arbitrage.infrastructure.event.dto.PremiumDto;
import main.arbitrage.application.collector.dto.TradeDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PremiumCalculationService {
    public PremiumDto calculatePremium(
            TradeDto domestic,
            TradeDto overseas,
            double usdToKrw,
            String symbol
    ) {
        BigDecimal domesticPriceUsd = CurrencyConverter.krwToUsd(
                domestic.getPrice(),
                usdToKrw
        );
        BigDecimal premium = FinancialCalculator.calculatePremium(
                domesticPriceUsd,
                overseas.getPrice()
        );

        return PremiumDto.builder()
                .symbol(symbol)
                .premium(premium)
                .domestic(domestic.getPrice())
                .overseas(overseas.getPrice())
                .usdToKrw(usdToKrw)
                .domesticTradeAt(domestic.getTimestamp())
                .overseasTradeAt(overseas.getTimestamp())
                .build();
    }
}