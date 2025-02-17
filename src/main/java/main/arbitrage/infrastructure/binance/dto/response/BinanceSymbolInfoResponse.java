package main.arbitrage.infrastructure.binance.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import main.arbitrage.infrastructure.binance.dto.enums.BinanceEnums.MarginType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BinanceSymbolInfoResponse(
    String symbol,
    MarginType marginType,
    String isAutoAddMargin,
    Integer leverage,
    String maxNotionalValue) {}
