package main.arbitrage.domain.exchangeRate.validator.common;

public interface Validation {
    boolean validate(double currentRate);
}