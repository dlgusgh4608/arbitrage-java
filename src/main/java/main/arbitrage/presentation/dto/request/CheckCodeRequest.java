package main.arbitrage.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CheckCodeRequest(
    @NotBlank(message = "originCode is empty") String originCode,
    @NotBlank(message = "encryptedCode is empty") String encryptedCode) {}
