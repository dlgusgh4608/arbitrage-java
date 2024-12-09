package main.arbitrage.global.util.calculator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CalculatorTest {
    @Nested
    @DisplayName("프리미엄 계산 테스트")
    class PremiumTest {
        @ParameterizedTest
        @CsvSource({
                "1000.0, 900.0, 11.1111",    // 양수 프리미엄
                "900.0, 1000.0, -10.0000",   // 음수 프리미엄
                "1000.0, 1000.0, 0.0000",    // 제로 프리미엄
                "0.5, 0.4, 25.0000",         // 소수점 케이스
                "100000.0, 90000.0, 11.1111" // 큰 숫자 케이스
        })
        @DisplayName("프리미엄 계산")
        void calculatePremium_Success(String domestic, String overseas, String expected) {
            // given
            BigDecimal domesticPrice = new BigDecimal(domestic);
            BigDecimal overseasPrice = new BigDecimal(overseas);
            BigDecimal expectedPremium = new BigDecimal(expected);

            // when
            BigDecimal result = FinancialCalculator.calculatePremium(domesticPrice, overseasPrice);

            // then
            assertThat(result).isEqualByComparingTo(expectedPremium);
        }
    }

    @Nested
    @DisplayName("무릎값 테스트")
    class KneeValueTest {

        @ParameterizedTest
        @CsvSource({
                "100.0, 80.0, 85.0000",     // 일반 케이스
                "100.0, 100.0, 100.0000",   // 최대값 = 최소값
                "0.0, 0.0, 0.0000",         // 0 케이스
                "1.5, 1.0, 1.1250",         // 소수점 케이스
                "10000.0, 8000.0, 8500.0000" // 큰 숫자 케이스
        })
        @DisplayName("최대값과 최소값으로 무릎값을 계산한다")
        void calculateKneeValue_Success(String max, String min, String expected) {
            // given
            BigDecimal maxValue = new BigDecimal(max);
            BigDecimal minValue = new BigDecimal(min);
            BigDecimal expectedValue = new BigDecimal(expected);

            // when
            BigDecimal result = FinancialCalculator.calculateKneeValue(maxValue, minValue);

            // then
            assertThat(result).isEqualByComparingTo(expectedValue);
        }
    }

    @Nested
    @DisplayName("어깨값 테스트")
    class ShoulderValueTest {

        @ParameterizedTest
        @CsvSource({
                "100.0, 80.0, 95.0000",     // 일반 케이스
                "100.0, 100.0, 100.0000",   // 최대값 = 최소값
                "0.0, 0.0, 0.0000",         // 0 케이스
                "1.5, 1.0, 1.3750",         // 소수점 케이스
                "10000.0, 8000.0, 9500.0000" // 큰 숫자 케이스
        })
        @DisplayName("최대값과 최소값으로 어깨값을 계산한다")
        void calculateShoulderValue_Success(String max, String min, String expected) {
            // given
            BigDecimal maxValue = new BigDecimal(max);
            BigDecimal minValue = new BigDecimal(min);
            BigDecimal expectedValue = new BigDecimal(expected);

            // when
            BigDecimal result = FinancialCalculator.calculateShoulderValue(maxValue, minValue);

            // then
            assertThat(result).isEqualByComparingTo(expectedValue);
        }
    }

    @Nested
    @DisplayName("퍼센트 테스트")
    class CalculatePercentageTest {

        @ParameterizedTest
        @CsvSource({
                "200.0, 50.0, 400.0000",
                "300.0, 150.0, 200.0000",
                "100.0, 100.0, 100.0000",
                "0.0, 50.0, 0.0000",
                "20, 100, 20.0000"
        })
        @DisplayName("정상적인 입력값으로 퍼센트를 계산한다")
        void calculatePercentage_Success(String total, String part, String expected) {
            BigDecimal totalValue = new BigDecimal(total);
            BigDecimal partValue = new BigDecimal(part);
            BigDecimal expectedValue = new BigDecimal(expected);

            BigDecimal result = PercentageCalculator.calculatePercentage(totalValue, partValue);

            assertThat(result).isEqualByComparingTo(expectedValue);
        }
    }

    @Test
    @DisplayName("null체크")
    void testNullInputs() {
        // given
        BigDecimal value = new BigDecimal("100.0");

        // when & then
        assertThatThrownBy(() ->
                FinancialCalculator.calculatePremium(null, value)
        ).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() ->
                FinancialCalculator.calculateKneeValue(null, value)
        ).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() ->
                FinancialCalculator.calculateShoulderValue(null, value)
        ).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() ->
                PercentageCalculator.calculatePercentage(null, value)
        ).isInstanceOf(NullPointerException.class);
    }
}
