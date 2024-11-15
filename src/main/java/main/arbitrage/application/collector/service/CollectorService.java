package main.arbitrage.application.collector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.domain.price.service.PriceDomainService;
import main.arbitrage.global.constant.SupportedSymbol;
import main.arbitrage.infrastructure.event.dto.PremiumDto;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.application.collector.dto.ExchangePair;
import main.arbitrage.infrastructure.event.EventEmitter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import main.arbitrage.global.util.json.TypedJsonNode;

import java.util.concurrent.CompletableFuture;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
public class CollectorService {
    private final ExchangeTradeCollector exchangeCollector;
    private final PremiumCalculationService premiumCalculator;
    private final TradeValidationService tradeValidator;
    private final EventEmitter emitter;
    private final ObjectMapper objectMapper;
    private final PriceDomainService priceDomainService;

    private TypedJsonNode<ExchangeRate> exchangeRate;

    @PostConstruct
    private void initialize() {
        emitter.on("updateUsdToKrw", data ->
                exchangeRate = TypedJsonNode.of(data, ExchangeRate.class)
        );

        exchangeCollector.initialize();
    }

    @Scheduled(fixedDelay = 1000) // 1초
    protected void processScheduler() {
        if (exchangeRate == null) return;
        ExchangeRate ex = exchangeRate.convertToType(objectMapper);
        processAllSymbols(ex);
    }

    @Scheduled(fixedDelay = 60 * 1000) // 1분
    @Transactional
    protected void saveBufferScheduler() {
        priceDomainService.saveBufferedData();
    }

    private void processAllSymbols(ExchangeRate exchangeRate) {
        List<CompletableFuture<Void>> futures = SupportedSymbol.getApplySymbols()
                .stream()
                .map(symbol -> calculateAndEmitPremiumAsync(exchangeRate, symbol))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    @Async
    private CompletableFuture<Void> calculateAndEmitPremiumAsync(ExchangeRate exchangeRate, String symbol) {
        return CompletableFuture.runAsync(() -> {
            try {
                ExchangePair tradePair = exchangeCollector.collectTrades(symbol);

                if (!tradeValidator.isValidTradePair(tradePair.getUpbit(), tradePair.getBinance())) {
                    log.warn("Invalid trade pair for symbol: {}", symbol);
                    return;
                }

                PremiumDto premium = premiumCalculator.calculatePremium(
                        tradePair.getUpbit(),
                        tradePair.getBinance(),
                        exchangeRate.getRate(),
                        symbol
                );

                priceDomainService.saveToBuffer(symbol, exchangeRate, tradePair, premium);
                emitPremium(symbol, premium);

            } catch (Exception e) {
                log.error("Error processing symbol {}: ", symbol, e);
            }
        });
    }


    private void emitPremium(String symbol, PremiumDto premium) {
        JsonNode payload = objectMapper.valueToTree(premium);

        System.out.println(payload.toString());
        emitter.emit(symbol, payload);
    }
}