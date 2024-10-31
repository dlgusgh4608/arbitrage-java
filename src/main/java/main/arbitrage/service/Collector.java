package main.arbitrage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.common.util.calculator.FinancialCalculator;
import main.arbitrage.common.util.currency.CurrencyConverter;
import main.arbitrage.infrastructure.event.EventEmitter;
import main.arbitrage.infrastructure.websocket.common.BaseWebSocketClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import main.arbitrage.infrastructure.websocket.common.dto.CommonTradeDto;
import main.arbitrage.infrastructure.websocket.common.dto.CommonOrderbookDto;
import main.arbitrage.common.dto.PremiumDto;

import java.math.BigDecimal;


@Service
@Slf4j
public class Collector {
    private final BaseWebSocketClient upbitWebSocket;
    private final BaseWebSocketClient binanceWebSocket;
    private final ObjectMapper objectMapper;
    private final EventEmitter emitter;

    private final FinancialCalculator financialCalculator;
    private final CurrencyConverter currencyConverter;

    private double usdToKrw;

    public Collector(
            @Qualifier("upbitWebSocket") BaseWebSocketClient upbitWebSocket,
            @Qualifier("binanceWebSocket") BaseWebSocketClient binanceWebSocket,
            EventEmitter emitter,
            ObjectMapper objectMapper
    ) {
        this.upbitWebSocket = upbitWebSocket;
        this.binanceWebSocket = binanceWebSocket;
        this.emitter = emitter;
        this.objectMapper = objectMapper;
        this.currencyConverter = new CurrencyConverter();
        this.financialCalculator = new FinancialCalculator();
    }

    @PostConstruct
    private void initialize() {
        emitter.on("updateUsdToKrw", data -> this.usdToKrw = data.asDouble());

        upbitWebSocket.connect();
        binanceWebSocket.connect();
    }

    @PostConstruct
    @Scheduled(fixedDelay = 500)
    private void publish() {
        if (usdToKrw == 0) return;

        makePremiumOfTrade();
    }

    private void makePremiumOfTrade() {
        CommonTradeDto upbitTrade = upbitWebSocket.getTrade("btc");
        CommonTradeDto binanceTrade = binanceWebSocket.getTrade("btc");

        if (upbitTrade == null || binanceTrade == null) return;

        Long binanceTimestamp = binanceTrade.getTimestamp();
        Long upbitTimestamp = upbitTrade.getTimestamp();

        // 각 거래소의 trade timestamp를 비교해서 30초 이상 차이 나면 정상적인 가격이 아니라 판단하고 return
        if (Math.abs(binanceTimestamp - upbitTimestamp) > 30000) return;

        BigDecimal upbitPrice = upbitTrade.getPrice();
        BigDecimal binancePrice = binanceTrade.getPrice();

        BigDecimal upbitPriceUsd = currencyConverter.krwToUsd(upbitPrice, usdToKrw);
        BigDecimal kimchiPremium = financialCalculator.calculatePremium(upbitPriceUsd, binancePrice);

        PremiumDto premium = PremiumDto.builder()
                .symbol("btc")
                .premium(kimchiPremium)
                .domestic(upbitPrice)
                .overseas(binancePrice)
                .usdToKrw(usdToKrw)
                .domesticTradeAt(upbitTimestamp)
                .overseasTradeAt(binanceTimestamp)
                .build();

        JsonNode payload = objectMapper.valueToTree(premium);

        System.out.println(payload);
        emitter.emit("btc", payload);
    }
}