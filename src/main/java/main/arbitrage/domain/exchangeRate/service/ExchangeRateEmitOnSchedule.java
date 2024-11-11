package main.arbitrage.domain.exchangeRate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.common.event.EventEmitter;
import main.arbitrage.domain.exchangeRate.controller.UsdToKrwValidation;
import main.arbitrage.domain.exchangeRate.infrastructure.exchangeRate.usdToKrw.UsdToKrw;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class ExchangeRateEmitOnSchedule {
    private final UsdToKrw usdToKrwCrawler;
    private final UsdToKrwValidation usdToKrwValidation;
    private final ObjectMapper objectMapper;
    private final EventEmitter emitter;

    @Scheduled(fixedDelay = 1000 * 10) // 10sec
    @PostConstruct
    private void emitOnSchedule() {
        double usdToKrw = usdToKrwCrawler.craw();

        if (!usdToKrwValidation.validate(usdToKrw)) return;

        log.info("[Update USD to KRW] {}", usdToKrw);

        JsonNode rateTojsonNode = objectMapper.valueToTree(usdToKrw);

        emitter.emit("updateUsdToKrw", rateTojsonNode);
    }
}