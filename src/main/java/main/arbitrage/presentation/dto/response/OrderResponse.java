package main.arbitrage.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;

@Getter
@Builder
public class OrderResponse {
  private final Long id;
  private final String symbol;
  private final float premium;
  private final double binanceAvgPrice;
  private final double binanceQty;
  private final double binanceTotalPrice;
  private final float binanceCommission;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
  private final LocalDateTime binanceEventTime;

  private final double upbitTotalPrice;
  private final double upbitQty;
  private final double upbitAvgPrice;
  private final float upbitCommission;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
  private final LocalDateTime upbitEventTime;

  private final float usdToKrw;
  private final boolean isMaker;
  private final boolean isClose;

  // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
  // private final LocalDateTime createdAt;

  private final List<SellOrders> sellOrders;

  @Getter
  @Builder
  public static class SellOrders {
    private final float premium;

    private final double binanceAvgPrice;
    private final double binanceQty;
    private final double binanceTotalPrice;
    private final float binanceCommission;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private final LocalDateTime binanceEventTime;

    private final double upbitTotalPrice;
    private final double upbitQty;
    private final double upbitAvgPrice;
    private final float upbitCommission;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private final LocalDateTime upbitEventTime;

    private final float usdToKrw;
    private final boolean isMaker;
    private final float profitRate;

    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    // private final LocalDateTime createdAt;
  }

  public static OrderResponse fromEntity(BuyOrder buyOrder) {
    return OrderResponse.builder()
        .id(buyOrder.getId())
        .symbol(buyOrder.getSymbol().getName())
        .premium(buyOrder.getPremium())
        .binanceAvgPrice(buyOrder.getBinancePrice())
        .binanceQty(buyOrder.getBinanceQuantity())
        .binanceTotalPrice(buyOrder.getBinancePrice() * buyOrder.getBinanceQuantity())
        .binanceCommission(buyOrder.getBinanceCommission())
        .binanceEventTime(buyOrder.getBinanceEventTime().toLocalDateTime())
        .upbitTotalPrice(buyOrder.getUpbitPrice() * buyOrder.getUpbitQuantity())
        .upbitQty(buyOrder.getUpbitQuantity())
        .upbitAvgPrice(buyOrder.getUpbitPrice())
        .upbitCommission(buyOrder.getUpbitCommission())
        .upbitEventTime(buyOrder.getUpbitEventTime().toLocalDateTime())
        .usdToKrw(buyOrder.getExchangeRate().getRate())
        .isMaker(buyOrder.isMaker())
        .isClose(buyOrder.isClose())
        .sellOrders(
            buyOrder.getSellOrders().stream()
                .map(
                    sellOrder ->
                        SellOrders.builder()
                            .premium(sellOrder.getPremium())
                            .binanceAvgPrice(sellOrder.getBinancePrice())
                            .binanceQty(sellOrder.getBinanceQuantity())
                            .binanceTotalPrice(
                                sellOrder.getBinancePrice() * sellOrder.getBinanceQuantity())
                            .binanceCommission(sellOrder.getBinanceCommission())
                            .binanceEventTime(sellOrder.getBinanceEventTime().toLocalDateTime())
                            .upbitTotalPrice(
                                sellOrder.getUpbitPrice() * sellOrder.getUpbitQuantity())
                            .upbitQty(sellOrder.getUpbitQuantity())
                            .upbitAvgPrice(sellOrder.getUpbitPrice())
                            .upbitCommission(sellOrder.getUpbitCommission())
                            .upbitEventTime(sellOrder.getUpbitEventTime().toLocalDateTime())
                            .usdToKrw(sellOrder.getExchangeRate().getRate())
                            .isMaker(sellOrder.isMaker())
                            .profitRate(sellOrder.getProfitRateWithFees())
                            .build())
                .toList())
        .build();
  }
}
