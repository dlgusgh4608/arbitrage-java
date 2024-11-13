package main.arbitrage.global.util.currency;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CurrencyConverter {
    public BigDecimal krwToUsd(BigDecimal krw, double exchangeRate) {
        return krw.divide(BigDecimal.valueOf(exchangeRate), 4, RoundingMode.HALF_UP);
    }

    public BigDecimal usdToKrw(BigDecimal usd, double exchangeRate) {
        return usd.multiply(BigDecimal.valueOf(exchangeRate)).setScale(0, RoundingMode.HALF_UP);
    }
}

