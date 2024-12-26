package main.arbitrage.presentation.dto.view;

import lombok.Builder;
import lombok.Getter;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.MarginType;

@Builder
@Getter
public class UserTradeInfo {
    private final Double usdt;
    private final Double krw;
    private final MarginType marginType;
    private final Integer leverage;
}
