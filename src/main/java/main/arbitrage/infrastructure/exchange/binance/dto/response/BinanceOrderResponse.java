package main.arbitrage.infrastructure.exchange.binance.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Side;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Status;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BinanceOrderResponse(
    String clientOrderId,
    String symbol,
    Side side,
    Status status,
    double price,
    double origQty,
    double avgPrice,
    double executedQty,
    double cumQuote,
    @JsonProperty("updateTime") Long eventTime) {}
