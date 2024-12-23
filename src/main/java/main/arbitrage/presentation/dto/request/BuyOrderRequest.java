package main.arbitrage.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BuyOrderRequest {
    @NotBlank(message = "symbol is required")
    private String symbol;

    @NotNull(message = "qty is required")
    private Double qty;
}
