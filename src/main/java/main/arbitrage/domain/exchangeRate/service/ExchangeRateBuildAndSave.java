package main.arbitrage.domain.exchangeRate.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.exchangeRate.repository.ExchangeRateRepository;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ExchangeRateBuildAndSave {
    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRate buildAndSave(String fromCurrency, String toCurrency, double rate) {
        ExchangeRate exchangeRate = ExchangeRate.builder()
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .rate(rate)
                .build();

        return exchangeRateRepository.save(exchangeRate);
    }
}