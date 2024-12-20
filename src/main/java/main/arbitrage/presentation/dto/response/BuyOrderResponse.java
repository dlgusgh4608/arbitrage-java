package main.arbitrage.presentation.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BuyOrderResponse {
    private double premium;
    private double binanceAvgPrice;
    private double binanceQty;
    private double binanceTotalPrice;
    private double binanceCommission;
    private double upbitTotalPrice;
    private double upbitQty;
    private double upbitAvgPrice;
    private double upbitCommission;
    private double usdToKrw;
}
