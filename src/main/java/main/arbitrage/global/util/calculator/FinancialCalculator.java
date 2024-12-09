package main.arbitrage.global.util.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FinancialCalculator {
    public static BigDecimal calculatePremium(BigDecimal domestic, BigDecimal overseas) {
        return domestic.divide(overseas, 8, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(new BigDecimal("100"))
                .setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateKneeValue(BigDecimal max, BigDecimal min) {
        return max.subtract(min)
                .multiply(new BigDecimal("0.25"))
                .add(min)
                .setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateShoulderValue(BigDecimal max, BigDecimal min) {
        return max.subtract(
                max.subtract(min)
                        .multiply(new BigDecimal("0.25"))
        ).setScale(4, RoundingMode.HALF_UP);
    }
}