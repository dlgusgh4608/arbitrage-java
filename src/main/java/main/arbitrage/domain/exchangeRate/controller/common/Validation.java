package main.arbitrage.domain.exchangeRate.controller.common;

public interface Validation {
    boolean validate(double currentRate);
}