package main.arbitrage.domain.sellOrder.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.order.dto.OrderCalcResult;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.sellOrder.entity.SellOrder;
import main.arbitrage.domain.sellOrder.exception.SellOrderErrorCode;
import main.arbitrage.domain.sellOrder.exception.SellOrderException;
import main.arbitrage.domain.sellOrder.repository.SellOrderRepository;
import main.arbitrage.global.util.math.MathUtil;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceOrderResponse;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitGetOrderResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellOrderService {
  private final double BINANCE_TAKER_COMM_RATE = 0.0005d;
  private final double UPBIT_COMM_RATE = 0.0005d;
  private final SellOrderRepository sellOrderRepository;

  public SellOrder createMarketOrder(
      OrderCalcResult orderCalcResult,
      BinanceOrderResponse binanceOrderResponse,
      UpbitGetOrderResponse upbitGetOrderResponse,
      ExchangeRate exchangeRate) {
    try {
      double binanceAvgPrice = binanceOrderResponse.avgPrice(); // 바이낸스 평단가
      double binanceQty = orderCalcResult.getBinanceQty().doubleValue(); // 바이낸스 체결 개수
      double binanceTotalPrice = binanceAvgPrice * binanceQty; // 바이낸스에 사용한 USDT
      double binanceCommission = MathUtil.roundTo(binanceTotalPrice * BINANCE_TAKER_COMM_RATE, 8);

      double upbitTotalPriceFromOutside =
          upbitGetOrderResponse.trades().stream()
              .mapToDouble(UpbitGetOrderResponse.Trade::funds)
              .sum();
      double upbitQtyFromOutside = upbitGetOrderResponse.executedVolume();
      double upbitAvgPrice = Math.round(upbitTotalPriceFromOutside / upbitQtyFromOutside);

      double upbitQty = orderCalcResult.getUpbitQty().doubleValue();
      double upbitTotalPrice = upbitAvgPrice * upbitQty;
      double upbitCommission = MathUtil.roundTo(upbitTotalPrice * UPBIT_COMM_RATE, 8);
      double usdToKrw = exchangeRate.getRate(); // 원달러 환율

      double premium = MathUtil.calculatePremium(upbitAvgPrice, binanceAvgPrice, usdToKrw);
      double exchangeRateAtBuy = orderCalcResult.getBuyOrder().getExchangeRate().getRate();
      double premiumInBuyOrder = orderCalcResult.getBuyOrder().getPremium();
      double premiumWithBuyExchangeRate =
          MathUtil.calculatePremium(upbitAvgPrice, binanceAvgPrice, exchangeRateAtBuy);

      double profitRate = MathUtil.roundTo(premiumInBuyOrder - premiumWithBuyExchangeRate, 4);

      SellOrder sellOrder =
          SellOrder.builder()
              .buyOrder(orderCalcResult.getBuyOrder())
              .exchangeRate(exchangeRate)
              .premium(premium)
              .upbitPrice(upbitAvgPrice)
              .upbitQuantity(upbitQty)
              .upbitCommission(upbitCommission)
              .binancePrice(binanceAvgPrice)
              .binanceQuantity(binanceQty)
              .binanceCommission(binanceCommission)
              .isMaker(false)
              .profitRate(profitRate)
              .build();

      return sellOrderRepository.save(sellOrder);
    } catch (Exception e) {
      String errorMessage =
          String.format(
              "매도 주문 생성 오류\ncalcResult: %s\nbinanceResponse: %s\nupbitResponse: %s",
              orderCalcResult, binanceOrderResponse, upbitGetOrderResponse);
      throw new SellOrderException(SellOrderErrorCode.UNKNOWN, errorMessage, e);
    }
  }

  public void calculateSellQty(
      List<OrderCalcResult> results, List<BuyOrder> openOrders, BigDecimal qty) {
    try {
      if (qty.equals(BigDecimal.ZERO)) throw new SellOrderException(SellOrderErrorCode.INVALID_QTY);

      BigDecimal upbitTotalQty = BigDecimal.ZERO;

      for (BuyOrder buyOrder : openOrders) {
        // qty가 0이면 break
        if (qty.signum() == 0) break;

        BigDecimal restBinanceQty = buyOrder.getRestBinanceQty();
        BigDecimal restUpbitQty = buyOrder.getRestUpbitQty();

        if (qty.compareTo(restBinanceQty) >= 0) {
          // 남은 수량이 현재 주문의 잔여 수량보다 크거나 같은 경우
          OrderCalcResult orderItem =
              OrderCalcResult.builder()
                  .buyOrder(buyOrder)
                  .binanceQty(restBinanceQty)
                  .upbitQty(restUpbitQty)
                  .isClose(true)
                  .build();

          upbitTotalQty = upbitTotalQty.add(restUpbitQty);
          results.add(orderItem);
          qty = qty.subtract(restBinanceQty);
        } else {
          // 남은 수량이 현재 주문의 잔여 수량보다 작은 경우
          // 잔여 수량에 대한 퍼센트를 이용해 upbit에 대한 qty를 구한 후 add
          BigDecimal percent = qty.divide(restBinanceQty, 8, RoundingMode.HALF_UP);
          BigDecimal calculatedUpbitQty =
              restUpbitQty.multiply(percent).setScale(8, RoundingMode.HALF_UP);

          OrderCalcResult orderItem =
              OrderCalcResult.builder()
                  .buyOrder(buyOrder)
                  .binanceQty(qty)
                  .upbitQty(calculatedUpbitQty)
                  .isClose(false)
                  .build();

          upbitTotalQty = upbitTotalQty.add(calculatedUpbitQty);
          results.add(orderItem);
          qty = BigDecimal.ZERO;
        }
      }

      if (qty.signum() < 0) throw new SellOrderException(SellOrderErrorCode.QTY_TO_LARGER);
    } catch (SellOrderException e) {
      throw e;
    } catch (Exception e) {
      throw new SellOrderException(SellOrderErrorCode.UNKNOWN, e);
    }
  }
}
