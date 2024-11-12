package main.arbitrage.domain.price.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.common.constant.SupportedSymbol;
import main.arbitrage.common.dto.PremiumDto;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.price.buffer.PriceBuffer;
import main.arbitrage.domain.price.controller.ExchangeTradeCollector;
import main.arbitrage.domain.price.controller.ExchangePair;
import main.arbitrage.domain.price.controller.TradeValidationService;
import main.arbitrage.common.event.EventEmitter;
import main.arbitrage.domain.price.entity.Price;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import main.arbitrage.common.util.json.TypedJsonNode;

import java.util.concurrent.CompletableFuture;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PremiumEmitOnSchedule {
    private final ExchangeTradeCollector exchangeCollector;
    private final PremiumCalculationService premiumCalculator;
    private final TradeValidationService tradeValidator;
    private final EventEmitter emitter;
    private final ObjectMapper objectMapper;
    private final PriceBuffer priceBuffer;

    private TypedJsonNode<ExchangeRate> exchangeRate;

    @PostConstruct
    private void initialize() {
        emitter.on("updateUsdToKrw", data ->
                exchangeRate = TypedJsonNode.of(data, ExchangeRate.class)
        );

        exchangeCollector.initialize();
    }

    @Scheduled(fixedDelay = 1000)
    private void scheduler() {
        if (exchangeRate == null) return;
        ExchangeRate ex = exchangeRate.convertToType(objectMapper);
        processAllSymbols(ex);
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

                saveToBuffer(symbol, exchangeRate, tradePair, premium);
                emitPremium(symbol, premium);

            } catch (Exception e) {
                log.error("Error processing symbol {}: ", symbol, e);
            }
        });
    }

    private void saveToBuffer(
            String symbol,
            ExchangeRate exchangeRate,
            ExchangePair tradePair,
            PremiumDto premium
    ) {
        Price symbolPrice = Price.builder()
                .symbol(symbol)
                .exchangeRate(exchangeRate)
                .premium(premium.getPremium())
                .upbit(tradePair.getUpbit().getPrice())
                .binance(tradePair.getBinance().getPrice())
                .upbitTradeAt(tradePair.getUpbit().getTimestamp())
                .binanceTradeAt(tradePair.getBinance().getTimestamp())
                .build();

        priceBuffer.add(symbolPrice);
    }

    private void emitPremium(String symbol, PremiumDto premium) {
        JsonNode payload = objectMapper.valueToTree(premium);
        System.out.println(payload.toString());

        emitter.emit(symbol, payload);
    }
}