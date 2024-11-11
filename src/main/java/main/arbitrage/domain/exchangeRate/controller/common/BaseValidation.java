package main.arbitrage.domain.exchangeRate.controller.common;

public abstract class BaseValidation implements Validation {
    protected double rate;

    @Override
    public abstract boolean validate(double currentRate);
}