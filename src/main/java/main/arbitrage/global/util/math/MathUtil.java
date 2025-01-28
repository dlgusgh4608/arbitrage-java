package main.arbitrage.global.util.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MathUtil {
  public static BigDecimal roundTo(BigDecimal value, int decimalPlaces) {
    return value.setScale(decimalPlaces, RoundingMode.HALF_UP);
  }

  public static BigDecimal floorTo(BigDecimal value, int decimalPlaces) {
    return value.setScale(decimalPlaces, RoundingMode.FLOOR);
  }

  public static BigDecimal ceilTo(BigDecimal value, int decimalPlaces) {
    return value.setScale(decimalPlaces, RoundingMode.CEILING);
  }

  public static BigDecimal roundTo(double value, int decimalPlaces) {
    return BigDecimal.valueOf(value).setScale(decimalPlaces, RoundingMode.HALF_UP);
  }

  public static BigDecimal floorTo(double value, int decimalPlaces) {
    return BigDecimal.valueOf(value).setScale(decimalPlaces, RoundingMode.FLOOR);
  }

  public static BigDecimal ceilTo(double value, int decimalPlaces) {
    return BigDecimal.valueOf(value).setScale(decimalPlaces, RoundingMode.CEILING);
  }

  // 평균값을 구할때에는 krw도 소수점이 존재할 수 있기에 double로 사용
  public static double krwToUsd(double krw, float exchangeRate) {
    return roundTo(krw / exchangeRate, 4).doubleValue();
  }

  public static double usdToKrw(double usd, float exchangeRate) {
    return roundTo(usd * exchangeRate, 0).doubleValue();
  }

  public static float calculatePremium(double upbit, double binance, float exchangeRate) {
    return roundTo((upbit / exchangeRate / binance - 1) * 100, 4).floatValue();
  }

  public static float calculatePercentValue(float min, float max, int percent) {
    return min + ((max - min) * (percent / 100));
  }

  public static int getDecimalPlaces(String value) {
    int dotIndex = value.indexOf(".");
    if (dotIndex == -1) return 0;

    return value.length() - dotIndex - 1;
  }

  public static int getDecimalPlaces(Double value) {
    String stringValue = String.valueOf(value);
    int dotIndex = stringValue.indexOf(".");
    if (dotIndex == -1) return 0;

    return stringValue.length() - dotIndex - 1;
  }
}
