package main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UpbitPostOrderRequestDto {
    private String symbol;
}