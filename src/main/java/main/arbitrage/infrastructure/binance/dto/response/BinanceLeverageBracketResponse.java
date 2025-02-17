package main.arbitrage.infrastructure.binance.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BinanceLeverageBracketResponse(String symbol, List<Brackets> brackets) {
  public record Brackets(
      Integer bracket,
      Integer initialLeverage,
      Long notionalCap,
      Long notionalFloor,
      Double maintMarginRatio,
      Double cum) {}
}
