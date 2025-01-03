package main.arbitrage.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.MarginType;

public record UpdateMarginTypeRequest(
    @NotEmpty(message = "symbol is not empty") String symbol,
    @NotNull(message = "marginType is not null") MarginType marginType) {}
