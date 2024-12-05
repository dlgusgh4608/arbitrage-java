package main.arbitrage.application.exchangeRate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateBuildAndSaveService;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateGetService;
import main.arbitrage.infrastructure.event.EventEmitter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {
    private final ExchangeRateBuildAndSaveService exchangeRateBuildAndSaveService;
    private final ExchangeRateGetService exchangeRateGetService;
    private final ObjectMapper objectMapper;
    private final EventEmitter emitter;

    @Scheduled(fixedDelay = 1000 * 10) // 10sec
    protected void scheduleOnEmit() {
        Double rate = exchangeRateGetService.get();

        if (rate == null) return;

        ExchangeRate exchangeRate = exchangeRateBuildAndSaveService
                .buildAndSave("USD", "KRW", rate);

        JsonNode rateTojsonNode = objectMapper.valueToTree(exchangeRate);

        emitter.emit("updateUsdToKrw", rateTojsonNode);
    }
}