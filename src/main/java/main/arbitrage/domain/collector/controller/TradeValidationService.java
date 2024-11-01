package main.arbitrage.domain.collector.controller;

import org.springframework.stereotype.Service;
import main.arbitrage.domain.collector.dto.TradeDto;

@Service
public class TradeValidationService {
    private static final long MAX_TIME_DIFFERENCE = 30000;

    public boolean isValidTradePair(TradeDto domestic, TradeDto overseas) {
        if (domestic == null || overseas == null) return false;

        return Math.abs(domestic.getTimestamp() - overseas.getTimestamp()) <= MAX_TIME_DIFFERENCE;
    }
}