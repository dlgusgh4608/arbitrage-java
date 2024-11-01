package main.arbitrage.domain.collector.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderbookDto {
    private String symbol;
    private OrderbookUnit[] bids = new OrderbookUnit[10];
    private OrderbookUnit[] asks = new OrderbookUnit[10];

    @Builder
    @Getter
    public static class OrderbookUnit {
        private BigDecimal price;
        private BigDecimal size;
    }
}