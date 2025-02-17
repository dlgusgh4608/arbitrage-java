package main.arbitrage.infrastructure.binance.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BinanceExchangeInfoResponse(
    Double maxQty, Double minQty, Double stepSize, Double minUsdt, Double tickSize) {
  public boolean isEmpty() {
    return maxQty == null
        || minQty == null
        || stepSize == null
        || minUsdt == null
        || tickSize == null;
  }
}
