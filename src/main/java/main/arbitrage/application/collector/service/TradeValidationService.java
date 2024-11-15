package main.arbitrage.application.collector.service;

import org.springframework.stereotype.Service;
import main.arbitrage.application.collector.dto.TradeDto;

@Service
public class TradeValidationService {
    private static final long MAX_TIME_DIFFERENCE = 30000;

    public boolean isValidTradePair(TradeDto upbit, TradeDto binance) {
        if (upbit == null || binance == null) return false;

        return Math.abs(upbit.getTimestamp() - binance.getTimestamp()) <= MAX_TIME_DIFFERENCE;
    }
}