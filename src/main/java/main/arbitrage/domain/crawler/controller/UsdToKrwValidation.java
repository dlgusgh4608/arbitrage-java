package main.arbitrage.domain.crawler.controller;

import main.arbitrage.domain.crawler.controller.common.BaseValidation;
import org.springframework.stereotype.Component;

@Component
public class UsdToKrwValidation extends BaseValidation {

    @Override
    public boolean validate(double currentRate) {
        if (currentRate == 0) return false;
        if (rate == currentRate) return false;

        rate = currentRate;

        return true;
    }
}