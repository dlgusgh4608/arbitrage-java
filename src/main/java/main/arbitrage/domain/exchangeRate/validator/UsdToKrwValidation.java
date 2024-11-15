package main.arbitrage.domain.exchangeRate.validator;

import main.arbitrage.domain.exchangeRate.validator.common.BaseValidation;
import org.springframework.stereotype.Service;

@Service
public class UsdToKrwValidation extends BaseValidation {

    @Override
    public boolean validate(double currentRate) {
        if (currentRate == 0) return false;
        if (rate == currentRate) return false;

        rate = currentRate;

        return true;
    }
}