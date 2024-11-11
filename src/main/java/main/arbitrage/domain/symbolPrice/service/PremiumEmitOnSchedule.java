package main.arbitrage.domain.symbolPrice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.common.dto.PremiumDto;
import main.arbitrage.domain.symbolPrice.controller.ExchangeTradeCollector;
import main.arbitrage.domain.symbolPrice.controller.ExchangePair;
import main.arbitrage.domain.symbolPrice.controller.TradeValidationService;
import main.arbitrage.common.event.EventEmitter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PremiumEmitOnSchedule {
    private final ExchangeTradeCollector exchangeCollector;
    private final PremiumCalculationService premiumCalculator;
    private final TradeValidationService tradeValidator;
    private final EventEmitter emitter;
    private final ObjectMapper objectMapper;

    private double usdToKrw;

    @PostConstruct
    private void initialize() {
        emitter.on("updateUsdToKrw", data -> this.usdToKrw = data.asDouble());
        exchangeCollector.initialize();
    }

    @PostConstruct
    @Scheduled(fixedDelay = 500)
    private void scheduler() {
        if (usdToKrw == 0) return;
        calculateAndEmitPremium();
    }

    private void calculateAndEmitPremium() {
        ExchangePair tradePair = exchangeCollector.collectTrades("btc");

        if (!tradeValidator.isValidTradePair(tradePair.getDomestic(), tradePair.getOverseas()))
            return;

        PremiumDto premium = premiumCalculator.calculatePremium(
                tradePair.getDomestic(),
                tradePair.getOverseas(),
                usdToKrw,
                "btc"
        );

        JsonNode payload = objectMapper.valueToTree(premium);
        System.out.println(payload.toString());

        emitter.emit("btc", payload);
    }
}