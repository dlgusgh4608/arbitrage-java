package main.arbitrage.presentation.dto.view;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.MarginType;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceLeverageBracketResponse;

@Builder
@Getter
public class UserTradeInfo {
    private final Double usdt;
    private final Double krw;
    private final MarginType marginType;
    private final Integer leverage;
    private final List<BinanceLeverageBracketResponse.Brackets> brackets;
}
