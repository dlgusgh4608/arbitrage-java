package main.arbitrage.domain.price.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.global.util.calculator.FinancialCalculator;
import main.arbitrage.global.util.currency.CurrencyConverter;
import main.arbitrage.application.event.dto.PremiumDto;
import main.arbitrage.domain.price.dto.TradeDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PremiumCalculationService {
    private final FinancialCalculator financialCalculator;
    private final CurrencyConverter currencyConverter;

    public PremiumDto calculatePremium(
            TradeDto domestic,
            TradeDto overseas,
            double usdToKrw,
            String symbol
    ) {
        BigDecimal domesticPriceUsd = currencyConverter.krwToUsd(
                domestic.getPrice(),
                usdToKrw
        );
        BigDecimal premium = financialCalculator.calculatePremium(
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