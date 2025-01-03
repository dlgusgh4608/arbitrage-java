package main.arbitrage.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateLeverageRequest(
    @NotEmpty(message = "symbol is not empty") String symbol,
    @NotNull(message = "leverage is not null") int leverage) {}
