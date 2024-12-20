package main.arbitrage.application.collector.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.infrastructure.exchange.factory.dto.OrderbookPair;

@Getter
@RequiredArgsConstructor
@Builder
public class ChartBySymbolDto {
    private final String symbol;
    private final PremiumDto premium;
    private final OrderbookPair orderbookPair;
}
