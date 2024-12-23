package main.arbitrage.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BuyOrderResponse {
    private final double premium;
    private final double binanceAvgPrice;
    private final double binanceQty;
    private final double binanceTotalPrice;
    private final double binanceCommission;
    private final double upbitTotalPrice;
    private final double upbitQty;
    private final double upbitAvgPrice;
    private final double upbitCommission;
    private final double usdToKrw;
}
