package main.arbitrage.infrastructure.websocket.common.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CommonOrderbookDto {
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