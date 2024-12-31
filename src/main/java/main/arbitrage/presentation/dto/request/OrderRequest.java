package main.arbitrage.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(@NotBlank(message = "type is Required!") String orderType,
                @NotBlank(message = "symbol is required") String symbol,
                @NotNull(message = "qty is required") Double qty) {
}
