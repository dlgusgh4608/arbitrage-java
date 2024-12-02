package main.arbitrage.application.collector.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.application.collector.dto.ChartDto;
import main.arbitrage.application.collector.dto.OrderbookPair;
import main.arbitrage.domain.price.entity.Price;
import main.arbitrage.domain.price.service.PriceDomainService;
import main.arbitrage.global.constant.SupportedSymbol;
import main.arbitrage.infrastructure.websocket.server.handler.ChartServerWebSocketHandler;
import main.arbitrage.infrastructure.websocket.server.handler.PremiumServerWebSocketHandler;
import main.arbitrage.infrastructure.event.dto.PremiumDto;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.application.collector.dto.TradePair;
import main.arbitrage.infrastructure.event.EventEmitter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import main.arbitrage.global.util.json.TypedJsonNode;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
    private final PremiumServerWebSocketHandler premiumServerWebSocketHandler;
    private final ChartServerWebSocketHandler chartServerWebSocketHandler;
    private final ConcurrentHashMap<String, Price> priceMap = new ConcurrentHashMap<>();

    private TypedJsonNode<ExchangeRate> exchangeRate;

    @PostConstruct
    private void initialize() {
        emitter.on("updateUsdToKrw", data ->
                exchangeRate = TypedJsonNode.of(data, ExchangeRate.class)
        );

        exchangeCollector.initialize();
    }

    @Scheduled(fixedRate = 300) // .3초
    protected void calculatePremium() {
        if (exchangeRate == null) return;
        ExchangeRate ex = exchangeRate.convertToType(objectMapper);
        processAllSymbols(ex);
    }

    @Scheduled(cron = "* * * * * *") // 1초
    protected void processScheduler() {
        priceMap.forEach((s, price) -> priceDomainService.addToBuffer(price));
    }

    @Scheduled(cron = "59 * * * * *") // 1분
    @Transactional
    protected void saveBufferScheduler() {
        priceDomainService.saveToPG();
    }

    private void processAllSymbols(ExchangeRate exchangeRate) {
        List<CompletableFuture<Void>> futures = SupportedSymbol.getApplySymbols()
                .stream()
                .map(symbol -> calculateAndEmitAsync(exchangeRate, symbol))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    @Async
    private CompletableFuture<Void> calculateAndEmitAsync(ExchangeRate exchangeRate, String symbol) {
        return CompletableFuture.runAsync(() -> {
            try {
                TradePair tradePair = exchangeCollector.collectTrades(symbol);
                OrderbookPair orderbookPair = exchangeCollector.collectOrderbooks(symbol);

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

                Price price = Price.builder()
                        .symbol(premium.getSymbol())
                        .exchangeRate(exchangeRate)
                        .premium(premium.getPremium())
                        .upbit(premium.getDomestic())
                        .binance(premium.getOverseas())
                        .upbitTradeAt(premium.getDomesticTradeAt())
                        .binanceTradeAt(premium.getOverseasTradeAt())
                        .build();


                priceMap.put(symbol, price);
                emitPremium(premium);
                emitOrderbook(symbol, premium, orderbookPair);
            } catch (Exception e) {
                log.error("Error processing symbol {}: ", symbol, e);
            }
        });
    }

    private void emitOrderbook(String symbol, PremiumDto premium, OrderbookPair orderbookPair) {
        chartServerWebSocketHandler.sendMessage(new ChartDto(symbol, premium, orderbookPair));
    }

    private void emitPremium(PremiumDto premium) {
        premiumServerWebSocketHandler.sendMessage(premium);
    }
}