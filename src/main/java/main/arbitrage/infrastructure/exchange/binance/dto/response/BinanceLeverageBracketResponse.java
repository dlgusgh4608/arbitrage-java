package main.arbitrage.infrastructure.exchange.binance.dto.response;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BinanceLeverageBracketResponse(String symbol, List<Brackets> brackets) {
    public record Brackets(Integer bracket, Integer initialLeverage, Long notionalCap,
            Long notionalFloor, Double maintMarginRatio, Double cum) {
    }
}

