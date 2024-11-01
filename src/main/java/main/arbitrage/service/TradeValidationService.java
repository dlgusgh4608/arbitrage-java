package main.arbitrage.service;

import org.springframework.stereotype.Service;
import main.arbitrage.infrastructure.websocket.common.dto.CommonTradeDto;

@Service
public class TradeValidationService {
    private static final long MAX_TIME_DIFFERENCE = 30000;

    public boolean isValidTradePair(CommonTradeDto domestic, CommonTradeDto overseas) {
        if (domestic == null || overseas == null) return false;

        return Math.abs(domestic.getTimestamp() - overseas.getTimestamp()) <= MAX_TIME_DIFFERENCE;
    }
}