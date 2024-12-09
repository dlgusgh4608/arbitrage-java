package main.arbitrage.global.util.currency;


import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CurrencyConverter {
    public static BigDecimal krwToUsd(BigDecimal krw, double exchangeRate) {
        return krw.divide(BigDecimal.valueOf(exchangeRate), 4, RoundingMode.HALF_UP);
    }

    public static BigDecimal usdToKrw(BigDecimal usd, double exchangeRate) {
        return usd.multiply(BigDecimal.valueOf(exchangeRate)).setScale(0, RoundingMode.HALF_UP);
    }
}

