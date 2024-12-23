package main.arbitrage.application.collector.dto;

import lombok.Builder;
import lombok.Getter;
import main.arbitrage.infrastructure.exchange.dto.OrderbookPair;

@Getter
@Builder
public class ChartBySymbolDTO {
    private final String symbol;
    private final PremiumDTO premium;
    private final OrderbookPair orderbookPair;
}
