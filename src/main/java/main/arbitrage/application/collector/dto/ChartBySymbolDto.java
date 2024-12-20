package main.arbitrage.application.collector.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.infrastructure.exchange.dto.OrderbookPair;

@Getter
@RequiredArgsConstructor
@Builder
public class ChartBySymbolDTO {
    private final String symbol;
    private final PremiumDTO premium;
    private final OrderbookPair orderbookPair;
}
