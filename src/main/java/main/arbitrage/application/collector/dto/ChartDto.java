package main.arbitrage.application.collector.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import main.arbitrage.infrastructure.event.dto.PremiumDto;

@Getter
@RequiredArgsConstructor
public class ChartDto {
    private final String symbol;
    private final PremiumDto premium;
    private final OrderbookPair orderbookPair;
}
