package main.arbitrage.domain.crawler.controller.common;

public abstract class BaseValidation implements Validation {
    protected double rate;

    @Override
    public abstract boolean validate(double currentRate);
}