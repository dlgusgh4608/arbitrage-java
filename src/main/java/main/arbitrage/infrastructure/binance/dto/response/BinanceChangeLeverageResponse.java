package main.arbitrage.infrastructure.binance.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BinanceChangeLeverageResponse(int leverage, String maxNotionalValue, String symbol) {}
