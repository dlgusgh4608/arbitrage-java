package main.arbitrage.infrastructure.exchange.binance.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BinancePositionInfoResponse(String entryPrice, String liquidationPrice,
        String positionAmt) {
}
// adl: 3
// askNotional: "0"
// bidNotional: "0"
// breakEvenPrice: "3424.18705"
// entryPrice: "3425.9"
// initialMargin: "4.01350800"
// isolatedMargin: "4.59472849"
// isolatedWallet: "4.10686849"
// liquidationPrice: "4094.00207337"
// maintMargin: "0.08027016"
// marginAsset: "USDT"
// markPrice: "3344.59000000"
// notional: "-20.06754000"
// openOrderInitialMargin: "0"
// positionAmt: "-0.006"
// positionInitialMargin: "4.01350800"
// positionSide: "BOTH"
// symbol: "ETHUSDT"
// unRealizedProfit: "0.48786000"
// updateTime: 1735603200500
