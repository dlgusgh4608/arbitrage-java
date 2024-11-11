package main.arbitrage.domain.symbolPrice.controller;

import org.springframework.stereotype.Service;
import main.arbitrage.domain.symbolPrice.dto.TradeDto;

@Service
public class TradeValidationService {
    private static final long MAX_TIME_DIFFERENCE = 30000;

    public boolean isValidTradePair(TradeDto domestic, TradeDto overseas) {
        if (domestic == null || overseas == null) return false;

        return Math.abs(domestic.getTimestamp() - overseas.getTimestamp()) <= MAX_TIME_DIFFERENCE;
    }
}