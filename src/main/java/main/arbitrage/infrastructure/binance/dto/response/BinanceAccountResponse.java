package main.arbitrage.infrastructure.binance.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BinanceAccountResponse(
    String accountAlias,
    String asset,
    String balance,
    String crossWalletBalance,
    String crossUnPnl,
    String availableBalance,
    String maxWithdrawAmount,
    Boolean marginAvailable,
    Long updateTime) {}
