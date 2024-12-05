package main.arbitrage.domain.exchangeRate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.domain.exchangeRate.validator.UsdToKrwValidation;
import main.arbitrage.infrastructure.crawler.UsdToKrw;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateGetService {
    private final UsdToKrw usdToKrwCrawler;
    private final UsdToKrwValidation usdToKrwValidation;

    public Double get() {
        double usdToKrw = usdToKrwCrawler.craw();

        if (!usdToKrwValidation.validate(usdToKrw)) return null;

        log.info("[Update USD to KRW] {}", usdToKrw);

        return usdToKrw;
    }
}