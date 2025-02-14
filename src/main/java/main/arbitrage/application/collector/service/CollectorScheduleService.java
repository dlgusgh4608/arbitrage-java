package main.arbitrage.application.collector.service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.application.collector.dto.ChartBySymbolDTO;
import main.arbitrage.application.collector.dto.PremiumDTO;
import main.arbitrage.application.collector.validator.TradeValidation;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
import main.arbitrage.domain.price.entity.Price;
import main.arbitrage.domain.price.service.PriceService;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.global.util.math.MathUtil;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceExchangeInfoResponse;
import main.arbitrage.infrastructure.exchange.binance.pub.rest.BinancePublicRestService;
import main.arbitrage.infrastructure.exchange.dto.OrderbookPair;
import main.arbitrage.infrastructure.exchange.dto.TradeDto;
import main.arbitrage.infrastructure.exchange.dto.TradePair;
import main.arbitrage.infrastructure.exchange.factory.ExchangePublicWebsocketFactory;
import main.arbitrage.infrastructure.websocket.handler.ChartWebsocketHandler;
import main.arbitrage.infrastructure.websocket.handler.PremiumWebsocketHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollectorScheduleService {
  private final SymbolVariableService symbolVariableService;
  private final ExchangePublicWebsocketFactory exchangePublicWebsocketFactory;
  private final TradeValidation tradeValidator;
  private final PriceService priceService;
  private final PremiumWebsocketHandler premiumWebsocketHandler;
  private final ChartWebsocketHandler chartWebsocketHandler;
  private final BinancePublicRestService binancePublicRestService;
  private final ExchangeRateService exchangeRateService;
  private final Map<String, Price> priceMap = new ConcurrentHashMap<>();
  private final ApplicationEventPublisher applicationEventPublisher;
  private Map<String, BinanceExchangeInfoResponse> binanceExchangeInfoMap = new HashMap<>();
  private ExecutorService threadPools;
  private List<Symbol> supportedSymbols = new ArrayList<>();

  @PostConstruct
  private void initialize() {
    exchangePublicWebsocketFactory.initialize();
    binanceExchangeInfoMap = binancePublicRestService.getExchangeInfo();
    supportedSymbols = symbolVariableService.getSupportedSymbols();
    threadPools = Executors.newFixedThreadPool((int) supportedSymbols.size() / 2);
  }

  @Scheduled(fixedRate = 300) // .3초
  protected void calculatePremium() {
    ExchangeRate exchangeRate = exchangeRateService.getUsdToKrw();
    if (exchangeRate == null) return;
    supportedSymbols.forEach(s -> this.processSymbol(s, exchangeRate));
  }

  @Scheduled(cron = "*/5 * * * * *") // 5초
  protected void processScheduler() {
    Date now = new Date();
    priceMap.forEach((s, price) -> priceService.addToBuffer(price.withCreatedAt(now)));
  }

  @Scheduled(cron = "59 * * * * *") // 1분
  @Transactional
  protected void saveBufferScheduler() {
    priceService.saveToPG();
  }

  @Scheduled(cron = "0 0 0 * * *") // 하루
  protected void updateExchangeInfo() {
    binanceExchangeInfoMap = binancePublicRestService.getExchangeInfo();
    applicationEventPublisher.publishEvent(binanceExchangeInfoMap);
  }

  private void processSymbol(Symbol symbol, ExchangeRate exchangeRate) {
    threadPools.execute(
        () -> {
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

            float exchangeRateValue = exchangeRate.getRate();
            double upbitPrc = upbitTrade.getPrice();
            double binancePrc = binanceTrade.getPrice();
            long upbitTradeAt = upbitTrade.getTimestamp();
            long binanceTradeAt = binanceTrade.getTimestamp();

            float premiumValue = MathUtil.calculatePremium(upbitPrc, binancePrc, exchangeRateValue);

            PremiumDTO premium =
                PremiumDTO.builder()
                    .symbol(symbolName)
                    .premium(premiumValue)
                    .upbit(upbitPrc)
                    .binance(binancePrc)
                    .usdToKrw(exchangeRateValue)
                    .upbitTradeAt(upbitTradeAt)
                    .binanceTradeAt(binanceTradeAt)
                    .build();

            Price price =
                Price.builder()
                    .symbol(symbol)
                    .exchangeRate(exchangeRate)
                    .premium(premium.getPremium())
                    .upbit(upbitPrc)
                    .binance(binancePrc)
                    .upbitTradeAt(upbitTradeAt)
                    .binanceTradeAt(binanceTradeAt)
                    .build();

            priceMap.put(symbolName, price); // domain 안의 buffer에 put -> 1분간격으로 db에 업로드
            emitPremium(premium);

            // 차트 데이터 빌드 후 websocket으로 전송 -> 따로 저장 안함.
            ChartBySymbolDTO chartBySymbolDto =
                ChartBySymbolDTO.builder()
                    .symbol(symbolName)
                    .premium(premium)
                    .orderbookPair(orderbookPair)
                    .build();

            emitChartBySymbol(chartBySymbolDto);
          } catch (Exception e) {
            log.error("Error processing symbol {}: ", symbol, e);
          }
        });
  }

  private void emitChartBySymbol(ChartBySymbolDTO chartBySymbolDto) {
    chartWebsocketHandler.sendMessage(chartBySymbolDto);
  }

  private void emitPremium(PremiumDTO premium) {
    premiumWebsocketHandler.sendMessage(premium);
    applicationEventPublisher.publishEvent(premium);
  }

  public BinanceExchangeInfoResponse getExchangeInfo(String symbol) {
    return binanceExchangeInfoMap.get(symbol);
  }
}
