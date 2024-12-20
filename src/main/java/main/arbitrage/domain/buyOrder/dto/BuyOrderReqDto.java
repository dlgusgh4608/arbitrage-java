package main.arbitrage.domain.buyOrder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BuyOrderReqDto {
    @NotBlank(message = "symbol is required")
    private String symbol;

    @NotNull(message = "qty is required")
    private Double qty;


}
