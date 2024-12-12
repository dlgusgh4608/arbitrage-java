package main.arbitrage.application.exchangeRate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.exchangeRate.dto.ExchangeRateDto;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
import main.arbitrage.infrastructure.crawler.UsdToKrwCrawler;
import main.arbitrage.infrastructure.event.EventEmitter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeRateApplicationService {
    private final UsdToKrwCrawler usdToKrwCrawler;
    private final ExchangeRateService exchangeRateService;
    private final ObjectMapper objectMapper;
    private final EventEmitter emitter;

    @Scheduled(fixedDelay = 1000 * 10) // 10sec
    protected void scheduleOnEmit() {
        double usdToKrw = usdToKrwCrawler.craw();

        ExchangeRateDto exchangeRateDto = ExchangeRateDto
                .builder()
                .fromCurrency("USD")
                .toCurrency("KRW")
                .rate(usdToKrw)
                .build();

        ExchangeRate exchangeRate = exchangeRateService.setExchangeRate(exchangeRateDto);

        if (exchangeRate == null) return;

        JsonNode rateTojsonNode = objectMapper.valueToTree(exchangeRate);
//
        emitter.emit("updateUsdToKrw", rateTojsonNode);
    }
}