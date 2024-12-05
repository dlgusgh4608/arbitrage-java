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
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.infrastructure.websocket.server.handler.ChartServerWebSocketHandler;
import main.arbitrage.infrastructure.websocket.server.handler.PremiumServerWebSocketHandler;
import main.arbitrage.infrastructure.event.dto.PremiumDto;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.application.collector.dto.TradePair;
import main.arbitrage.infrastructure.event.EventEmitter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import main.arbitrage.global.util.json.TypedJsonNode;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollectorScheduleService {
    private final SymbolVariableService symbolVariableService;
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
        Date now = new Date();
        priceMap.forEach((s, price) -> priceDomainService.addToBuffer(price.withCreatedAt(now)));
    }

    @Scheduled(cron = "59 * * * * *") // 1분
    @Transactional
    protected void saveBufferScheduler() {
        priceDomainService.saveToPG();
    }

    private void processAllSymbols(ExchangeRate exchangeRate) {
        List<CompletableFuture<Void>> futures = symbolVariableService.getSupportedSymbols()
                .stream()
                .map(symbol -> calculateAndEmitAsync(exchangeRate, symbol))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    @Async
    private CompletableFuture<Void> calculateAndEmitAsync(ExchangeRate exchangeRate, Symbol symbol) {
        return CompletableFuture.runAsync(() -> {
            try {
                String symbolName = symbol.getName();
                TradePair tradePair = exchangeCollector.collectTrades(symbolName);
                OrderbookPair orderbookPair = exchangeCollector.collectOrderbooks(symbolName);

                if (!tradeValidator.isValidTradePair(tradePair.getUpbit(), tradePair.getBinance())) {
                    log.warn("Invalid trade pair for symbol: {}", symbolName);
                    return;
                }

                PremiumDto premium = premiumCalculator.calculatePremium(
                        tradePair.getUpbit(),
                        tradePair.getBinance(),
                        exchangeRate.getRate(),
                        symbolName
                );

                Price price = Price.builder()
                        .symbol(symbol)
                        .exchangeRate(exchangeRate)
                        .premium(premium.getPremium())
                        .upbit(premium.getDomestic())
                        .binance(premium.getOverseas())
                        .upbitTradeAt(premium.getDomesticTradeAt())
                        .binanceTradeAt(premium.getOverseasTradeAt())
                        .build();

                priceMap.put(symbolName, price);
                emitPremium(premium);
                emitOrderbook(symbolName, premium, orderbookPair);
            } catch (Exception e) {
                log.error("Error processing symbol {}: ", symbol, e);
            }
        });
    }

    private void emitOrderbook(String symbolName, PremiumDto premium, OrderbookPair orderbookPair) {
        chartServerWebSocketHandler.sendMessage(new ChartDto(symbolName, premium, orderbookPair));
    }

    private void emitPremium(PremiumDto premium) {
        premiumServerWebSocketHandler.sendMessage(premium);
    }
}