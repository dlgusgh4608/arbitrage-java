package main.arbitrage.infrastructure.upbit.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UpbitAccountResponse(
    String currency,
    String balance,
    String locked,
    String avgBuyPrice,
    Boolean avgBuyPriceModified,
    String unitCurrency) {}
