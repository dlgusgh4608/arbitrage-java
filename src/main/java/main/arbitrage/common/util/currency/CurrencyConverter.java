package main.arbitrage.common.util.currency;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CurrencyConverter {
    public BigDecimal krwToUsd(BigDecimal krw, BigDecimal exchangeRate) {
        return krw.divide(exchangeRate, 4, RoundingMode.HALF_UP);
    }

    public BigDecimal usdToKrw(BigDecimal usd, BigDecimal exchangeRate) {
        return usd.multiply(exchangeRate).setScale(0, RoundingMode.HALF_UP);
    }
}

