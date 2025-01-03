package main.arbitrage.global.util.math;

public final class MathUtil {
    public static double roundTo(double value, int decimalPlaces) {
        return Math.round(value * Math.pow(10, decimalPlaces)) / Math.pow(10, decimalPlaces);
    }

    public static double floorTo(double value, int decimalPlaces) {
        return Math.floor(value * Math.pow(10, decimalPlaces)) / Math.pow(10, decimalPlaces);
    }

    public static double ceilTo(double value, int decimalPlaces) {
        return Math.ceil(value * Math.pow(10, decimalPlaces)) / Math.pow(10, decimalPlaces);
    }

    // 평균값을 구할때에는 krw도 소수점이 존재할 수 있기에 double로 사용
    public static double krwToUsd(double krw, double exchangeRate) {
        return roundTo(krw / exchangeRate, 4);
    }

    public static double usdToKrw(double usd, double exchangeRate) {
        return roundTo(usd * exchangeRate, 0);
    }

    public static double calculatePremium(double upbit, double binance, double exchangeRate) {
        return roundTo((upbit / exchangeRate / binance - 1) * 100, 4);
    }
}
