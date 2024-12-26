package main.arbitrage.infrastructure.exchange.binance.dto.response;

import lombok.Builder;
import lombok.Getter;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.MarginType;

@Getter
@Builder
public class BinanceSymbolInfoResponse {
    private final String symbol;
    private final MarginType marginType;
    private final String isAutoAddMargin;
    private final Integer leverage;
    private final String maxNotionalValue;
}
