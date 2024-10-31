package main.arbitrage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.infrastructure.event.EventEmitter;
import main.arbitrage.infrastructure.websocket.common.BaseWebSocketClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import main.arbitrage.infrastructure.websocket.common.dto.CommonTradeDto;
import main.arbitrage.infrastructure.websocket.common.dto.CommonOrderbookDto;

import java.math.BigDecimal;


@Service
@Slf4j
public class Collector {
    private final BaseWebSocketClient upbitWebSocket;
    private final BaseWebSocketClient binanceWebSocket;
    private final ObjectMapper objectMapper;
    private final EventEmitter emitter;

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

        CommonTradeDto upbitTrade = upbitWebSocket.getTrade("btc");
        CommonTradeDto binanceTrade = binanceWebSocket.getTrade("btc");

        if (upbitTrade == null || binanceTrade == null) return;
        
        System.out.println("다음 커밋에 만나요~");
    }

//    private makePremium() {
//
//    }
}