package main.arbitrage.presentation.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;

@Getter
@Builder
@ToString
public class BuyOrderResponse {
    private final Long id;
    private final String symbol;
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
    private final boolean isMaker;
    private final boolean isClose;
    private final List<SellOrderResponse> sellOrders;

    @Getter
    @Builder
    @ToString
    public static class SellOrderResponse {
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
        private final boolean isMaker;
        private final double profitRate;
    }

    public static BuyOrderResponse fromEntity(BuyOrder buyOrder) {
        return BuyOrderResponse.builder().id(buyOrder.getId())
                .symbol(buyOrder.getSymbol().getName()).premium(buyOrder.getPremium())
                .binanceAvgPrice(buyOrder.getBinancePrice())
                .binanceQty(buyOrder.getBinanceQuantity())
                .binanceTotalPrice(buyOrder.getBinancePrice() * buyOrder.getBinanceQuantity())
                .binanceCommission(buyOrder.getBinanceCommission())
                .upbitTotalPrice(buyOrder.getUpbitPrice() * buyOrder.getUpbitQuantity())
                .upbitQty(buyOrder.getUpbitQuantity()).upbitAvgPrice(buyOrder.getUpbitPrice())
                .upbitCommission(buyOrder.getUpbitCommission())
                .usdToKrw(buyOrder.getExchangeRate().getRate()).isMaker(buyOrder.isMaker())
                .isClose(buyOrder.isClose())
                .sellOrders(buyOrder.getSellOrders().stream().map(sellOrder -> SellOrderResponse
                        .builder().premium(sellOrder.getPremium())
                        .binanceAvgPrice(sellOrder.getBinancePrice())
                        .binanceQty(sellOrder.getBinanceQuantity())
                        .binanceTotalPrice(
                                sellOrder.getBinancePrice() * sellOrder.getBinanceQuantity())
                        .binanceCommission(sellOrder.getBinanceCommission())
                        .upbitTotalPrice(sellOrder.getUpbitPrice() * sellOrder.getUpbitQuantity())
                        .upbitQty(sellOrder.getUpbitQuantity())
                        .upbitAvgPrice(sellOrder.getUpbitPrice())
                        .upbitCommission(sellOrder.getUpbitCommission())
                        .usdToKrw(sellOrder.getExchangeRate().getRate())
                        .isMaker(sellOrder.isMaker()).profitRate(sellOrder.getProfitRate()).build())
                        .toList())
                .build();
    }
}
