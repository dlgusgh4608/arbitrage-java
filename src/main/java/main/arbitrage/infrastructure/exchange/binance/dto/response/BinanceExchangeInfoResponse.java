package main.arbitrage.infrastructure.exchange.binance.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BinanceExchangeInfoResponse(double maxQty, double minQty, double stepSize) {
}
