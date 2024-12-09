package main.arbitrage.global.util.calculator;


import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PercentageCalculator {

    public static BigDecimal calculatePercentage(BigDecimal total, BigDecimal part) {
        return total.divide(part, 8, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(4, RoundingMode.HALF_UP);
    }
}