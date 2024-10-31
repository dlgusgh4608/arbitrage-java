package main.arbitrage.common.util.calculator;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PercentageCalculator {

    public BigDecimal calculatePercentage(BigDecimal total, BigDecimal part) {
        return total.divide(part, 8, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal calculatePercentageAmount(BigDecimal amount, BigDecimal percent, int scale) {
        return percent.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP)
                .multiply(amount)
                .setScale(scale, RoundingMode.HALF_UP);
    }
}