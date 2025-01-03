package main.arbitrage.application.collector.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.application.collector.dto.ChartBySymbolDTO;
import main.arbitrage.application.collector.dto.PremiumDTO;
import main.arbitrage.application.collector.validator.TradeValidation;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.price.entity.Price;
import main.arbitrage.domain.price.service.PriceDomainService;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.global.util.math.MathUtil;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceExchangeInfoResponse;
import main.arbitrage.infrastructure.exchange.binance.pub.rest.BinancePublicRestService;
import main.arbitrage.infrastructure.exchange.dto.OrderbookPair;
import main.arbitrage.infrastructure.exchange.dto.TradeDto;
import main.arbitrage.infrastructure.exchange.dto.TradePair;
import main.arbitrage.infrastructure.exchange.factory.ExchangePublicWebsocketFactory;
import main.arbitrage.infrastructure.websocket.server.handler.ChartServerWebSocketHandler;
import main.arbitrage.infrastructure.websocket.server.handler.PremiumServerWebSocketHandler;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollectorScheduleService {
    private final SymbolVariableService symbolVariableService;
    private final ExchangePublicWebsocketFactory exchangePublicWebsocketFactory;
    private final TradeValidation tradeValidator;
    private final PriceDomainService priceDomainService;
    private final PremiumServerWebSocketHandler premiumServerWebSocketHandler;
    private final ChartServerWebSocketHandler chartServerWebSocketHandler;
    private final BinancePublicRestService binancePublicRestService;
    private final Map<String, Price> priceMap = new ConcurrentHashMap<>();
    private Map<String, BinanceExchangeInfoResponse> binanceExchangeInfoMap = new HashMap<>();

    private ExchangeRate exchangeRate;

    @EventListener
    public void customExchangeRate(ExchangeRate rate) {
        exchangeRate = rate;
    }

    @PostConstruct
    private void initialize() {
        exchangePublicWebsocketFactory.initialize();
        binanceExchangeInfoMap = binancePublicRestService.getExchangeInfo();
    }

    @Scheduled(fixedRate = 300) // .3초
    protected void calculatePremium() {
        if (exchangeRate == null)
            return;
        processAllSymbols(exchangeRate);
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

    @Scheduled(cron = "0 0 0 * * *") // 하루
    protected void updateExchangeInfo() {
        binanceExchangeInfoMap = binancePublicRestService.getExchangeInfo();
    }

    private void processAllSymbols(ExchangeRate exchangeRate) {
        // supported symbol은 upbit와 binance가 실행될때 동일하게 들어감.
        List<CompletableFuture<Void>> futures = symbolVariableService.getSupportedSymbols().stream()
                .map(symbol -> calculateAndEmitAsync(exchangeRate, symbol)).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    @Async
    private CompletableFuture<Void> calculateAndEmitAsync(ExchangeRate exchangeRate,
            Symbol symbol) {
        return CompletableFuture.runAsync(() -> {
            try {
                String symbolName = symbol.getName();

                TradePair tradePair = exchangePublicWebsocketFactory.collectTrades(symbolName);
                OrderbookPair orderbookPair =
                        exchangePublicWebsocketFactory.collectOrderbooks(symbolName);

                TradeDto upbitTrade = tradePair.getUpbit();
                TradeDto binanceTrade = tradePair.getBinance();

                if (!tradeValidator.isValidTradePair(upbitTrade, binanceTrade)) {
                    log.warn("Invalid trade pair for symbol: {}", symbolName);
                    return;
                }

                double exchangeRateValue = exchangeRate.getRate();
                double upbitPrc = upbitTrade.getPrice();
                double binancePrc = binanceTrade.getPrice();
                long upbitTradeAt = upbitTrade.getTimestamp();
                long binanceTradeAt = binanceTrade.getTimestamp();

                double premiumValue =
                        MathUtil.calculatePremium(upbitPrc, binancePrc, exchangeRateValue);

                PremiumDTO premium = PremiumDTO.builder().symbol(symbolName).premium(premiumValue)
                        .upbit(upbitPrc).binance(binancePrc).usdToKrw(exchangeRateValue)
                        .upbitTradeAt(upbitTradeAt).binanceTradeAt(binanceTradeAt).build();

                Price price = Price.builder().symbol(symbol).exchangeRate(exchangeRate)
                        .premium(premium.getPremium()).upbit(upbitPrc).binance(binancePrc)
                        .upbitTradeAt(upbitTradeAt).binanceTradeAt(binanceTradeAt).build();

                priceMap.put(symbolName, price); // domain 안의 buffer에 put -> 1분간격으로 db에 업로드
                emitPremium(premium);


                // 차트 데이터 빌드 후 websocket으로 전송 -> 따로 저장 안함.
                ChartBySymbolDTO chartBySymbolDto = ChartBySymbolDTO.builder().symbol(symbolName)
                        .premium(premium).orderbookPair(orderbookPair).build();

                emitChartBySymbol(chartBySymbolDto);
            } catch (Exception e) {
                log.error("Error processing symbol {}: ", symbol, e);
            }
        });
    }

    private void emitChartBySymbol(ChartBySymbolDTO chartBySymbolDto) {
        chartServerWebSocketHandler.sendMessage(chartBySymbolDto);
    }

    private void emitPremium(PremiumDTO premium) {
        premiumServerWebSocketHandler.sendMessage(premium);
    }

    public BinanceExchangeInfoResponse getExchangeInfo(String symbol) {
        return binanceExchangeInfoMap.get(symbol);
    }
}
