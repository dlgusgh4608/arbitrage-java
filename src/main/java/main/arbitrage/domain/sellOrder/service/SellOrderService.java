package main.arbitrage.domain.sellOrder.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.order.dto.OrderCalcResultDTO;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.sellOrder.dto.ProfitRateDTO;
import main.arbitrage.domain.sellOrder.entity.SellOrder;
import main.arbitrage.domain.sellOrder.exception.SellOrderErrorCode;
import main.arbitrage.domain.sellOrder.exception.SellOrderException;
import main.arbitrage.domain.sellOrder.repository.SellOrderRepository;
import main.arbitrage.global.util.math.MathUtil;
import main.arbitrage.infrastructure.exchange.binance.dto.event.BinanceOrderTradeUpdateEvent;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceOrderResponse;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitOrderResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellOrderService {
  private final double BINANCE_TAKER_COMM_RATE = 0.0005d;
  private final double UPBIT_COMM_RATE = 0.0005d;
  private final SellOrderRepository sellOrderRepository;

  public SellOrder createMarketOrder(
      OrderCalcResultDTO orderCalcResult,
      BinanceOrderResponse binanceOrderResponse,
      UpbitOrderResponse upbitOrderResponse,
      ExchangeRate exchangeRate) {
    try {
      double binanceAvgPrice = binanceOrderResponse.avgPrice(); // 바이낸스 평단가
      double binanceQty = orderCalcResult.getBinanceQty().doubleValue(); // 바이낸스 체결 개수
      double binanceTotalPrice = binanceAvgPrice * binanceQty; // 바이낸스에 사용한 USDT
      float binanceCommission =
          MathUtil.roundTo(binanceTotalPrice * BINANCE_TAKER_COMM_RATE, 8).floatValue(); // 바이낸스 수수료

      /** 업비트 API Response의 데이터를 이용해 평단가를 구함. */
      double upbitTotalPriceFromAPI =
          upbitOrderResponse.trades().stream().mapToDouble(UpbitOrderResponse.Trade::funds).sum();
      double upbitQtyFromAPI = upbitOrderResponse.executedVolume();
      double upbitAvgPrice =
          MathUtil.roundTo(upbitTotalPriceFromAPI / upbitQtyFromAPI, 8).doubleValue();

      /** 계산된 qty와 평단가를 합쳐 개수에 알맞은 수수료를 구함 */
      double upbitQty = orderCalcResult.getUpbitQty().doubleValue();
      double upbitTotalPrice = upbitAvgPrice * upbitQty;
      float upbitCommission =
          MathUtil.roundTo(upbitTotalPrice * UPBIT_COMM_RATE, 8).floatValue(); // 업비트 수수료

      /** 현재 환율과 평단가를 통해 프리미엄 구함 */
      float usdToKrw = exchangeRate.getRate();
      float premium = MathUtil.calculatePremium(upbitAvgPrice, binanceAvgPrice, usdToKrw);

      ProfitRateDTO profitRateDTO =
          calculateProfitRate(
              orderCalcResult,
              binanceTotalPrice,
              binanceCommission,
              upbitTotalPrice,
              upbitCommission,
              usdToKrw);

      SellOrder sellOrder =
          SellOrder.builder()
              .buyOrder(orderCalcResult.getBuyOrder())
              .exchangeRate(exchangeRate)
              .premium(premium)
              .upbitPrice(upbitAvgPrice)
              .upbitQuantity(upbitQty)
              .upbitCommission(upbitCommission)
              .upbitEventTime(upbitOrderResponse.eventTime())
              .binancePrice(binanceAvgPrice)
              .binanceQuantity(binanceQty)
              .binanceCommission(binanceCommission)
              .binanceEventTime(binanceOrderResponse.eventTime())
              .isMaker(false)
              .profitRate(profitRateDTO.getProfitRate())
              .profitRateWithFees(profitRateDTO.getProfitRateWithFees())
              .build();

      return sellOrderRepository.save(sellOrder);
    } catch (Exception e) {
      String errorMessage =
          String.format(
              "매도 주문 생성 오류(시장가)\ncalcResult: %s\nbinanceResponse: %s\nupbitResponse: %s",
              orderCalcResult, binanceOrderResponse, upbitOrderResponse);
      throw new SellOrderException(SellOrderErrorCode.UNKNOWN, errorMessage, e);
    }
  }

  public SellOrder createLimitOrder(
      OrderCalcResultDTO orderCalcResult,
      BinanceOrderTradeUpdateEvent binanceOrderResponse,
      UpbitOrderResponse upbitOrderResponse,
      ExchangeRate exchangeRate) {
    try {
      double binanceAvgPrice = binanceOrderResponse.getPrice(); // 바이낸스 평단가
      double binanceQty = orderCalcResult.getBinanceQty().doubleValue(); // 바이낸스 체결 개수
      double binanceTotalPrice = binanceAvgPrice * binanceQty;
      double binanceTotalQty = binanceOrderResponse.getQuantity();
      float binanceTotalCommission = binanceOrderResponse.getCommission();

      // 바이낸스 수수료
      float binanceCommission =
          MathUtil.roundTo(binanceQty / binanceTotalQty * binanceTotalCommission, 8).floatValue();

      /** 업비트 API Response의 데이터를 이용해 평단가를 구함. */
      double upbitTotalPriceFromAPI =
          upbitOrderResponse.trades().stream().mapToDouble(UpbitOrderResponse.Trade::funds).sum();
      double upbitQtyFromAPI = upbitOrderResponse.executedVolume();
      double upbitAvgPrice =
          MathUtil.roundTo(upbitTotalPriceFromAPI / upbitQtyFromAPI, 8).doubleValue();

      /** 계산된 qty와 평단가를 합쳐 개수에 알맞은 수수료를 구함 */
      double upbitQty = orderCalcResult.getUpbitQty().doubleValue();
      double upbitTotalPrice = upbitAvgPrice * upbitQty;
      float upbitCommission =
          MathUtil.roundTo(upbitTotalPrice * UPBIT_COMM_RATE, 8).floatValue(); // 업비트 수수료

      /** 현재 환율과 평단가를 통해 프리미엄 구함 */
      float usdToKrw = exchangeRate.getRate();
      float premium = MathUtil.calculatePremium(upbitAvgPrice, binanceAvgPrice, usdToKrw);

      ProfitRateDTO profitRateDTO =
          calculateProfitRate(
              orderCalcResult,
              binanceTotalPrice,
              binanceCommission,
              upbitTotalPrice,
              upbitCommission,
              usdToKrw);

      SellOrder sellOrder =
          SellOrder.builder()
              .buyOrder(orderCalcResult.getBuyOrder())
              .exchangeRate(exchangeRate)
              .premium(premium)
              .upbitPrice(upbitAvgPrice)
              .upbitQuantity(upbitQty)
              .upbitCommission(upbitCommission)
              .upbitEventTime(upbitOrderResponse.eventTime())
              .binancePrice(binanceAvgPrice)
              .binanceQuantity(binanceQty)
              .binanceCommission(binanceCommission)
              .binanceEventTime(binanceOrderResponse.getEventTime())
              .isMaker(binanceOrderResponse.getIsMaker())
              .profitRate(profitRateDTO.getProfitRate())
              .profitRateWithFees(profitRateDTO.getProfitRateWithFees())
              .build();

      return sellOrderRepository.save(sellOrder);
    } catch (Exception e) {
      String errorMessage =
          String.format(
              "매도 주문 생성 오류(지정가)\ncalcResult: %s\nbinanceResponse: %s\nupbitResponse: %s",
              orderCalcResult, binanceOrderResponse, upbitOrderResponse);
      throw new SellOrderException(SellOrderErrorCode.UNKNOWN, errorMessage, e);
    }
  }

  private ProfitRateDTO calculateProfitRate(
      OrderCalcResultDTO orderCalcResult,
      double binanceTotalPriceOfSell,
      float binanceCommissionOfSell,
      double upbitTotalPriceOfSell,
      float upbitCommissionOfSell,
      float exchangeRateOfSell) {
    BuyOrder buyOrder = orderCalcResult.getBuyOrder();
    float exchangeRateOfBuy = buyOrder.getExchangeRate().getRate();

    double binanceExecRate =
        orderCalcResult.getBinanceQty().doubleValue() / buyOrder.getBinanceQuantity();
    double upbitExecRate =
        orderCalcResult.getUpbitQty().doubleValue() / buyOrder.getUpbitQuantity();

    // [매수] 구매당시 가격으로 판매한 개수만큼 총액과 수수료를 구함 (바이낸스)
    double binanceTotalPriceToKrwOfBuy =
        buyOrder.getBinancePrice()
            * buyOrder.getBinanceQuantity()
            * exchangeRateOfBuy
            * binanceExecRate;
    float binanceCommissionToKrwOfBuy =
        buyOrder.getBinanceCommission() * (float) exchangeRateOfBuy * (float) binanceExecRate;

    // [매수] 구매당시 가격으로 판매한 개수만큼 총액과 수수료를 구함 (업비트)
    double upbitTotalPriceOfBuy =
        buyOrder.getUpbitPrice() * buyOrder.getUpbitQuantity() * upbitExecRate;
    float upbitCommissionOfBuy = buyOrder.getUpbitCommission() * (float) upbitExecRate;

    // [매도] 현재 환율로 총액과 수수료를 구함 (바이낸스)
    double binanceTotalPriceToKrwOfSell = binanceTotalPriceOfSell * exchangeRateOfSell;
    float binanceCommissionToKrwOfSell = binanceCommissionOfSell * exchangeRateOfSell;

    // [매수, 매도] 수수료 제외 총 금액
    double totalBuyCost = binanceTotalPriceToKrwOfBuy + upbitTotalPriceOfBuy;
    double totalSellCost = binanceTotalPriceToKrwOfSell + upbitTotalPriceOfSell;

    // [매수, 매도] 수수료 포함 총 금액
    double totalBuyCostWithFees = totalBuyCost + binanceCommissionToKrwOfBuy + upbitCommissionOfBuy;

    double totalSellCostWithFees =
        totalSellCost - binanceCommissionToKrwOfSell - upbitCommissionOfSell;

    // [매수, 매도] 수익률 ((매도 총액 - 매수 총액) / 매수 총액) * 100
    float profitRate =
        MathUtil.roundTo(((totalSellCost - totalBuyCost) / totalBuyCost) * 100, 4).floatValue();

    float profitRateWithFees =
        MathUtil.roundTo(
                ((totalSellCostWithFees - totalBuyCostWithFees) / totalBuyCostWithFees) * 100, 4)
            .floatValue();

    return ProfitRateDTO.builder()
        .profitRate(profitRate)
        .profitRateWithFees(profitRateWithFees)
        .build();
  }

  public double calculateSellQty(
      List<OrderCalcResultDTO> results, List<BuyOrder> openOrders, BigDecimal qty) {
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
          OrderCalcResultDTO orderItem =
              OrderCalcResultDTO.builder()
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

          OrderCalcResultDTO orderItem =
              OrderCalcResultDTO.builder()
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

      return upbitTotalQty.doubleValue();
    } catch (SellOrderException e) {
      throw e;
    } catch (Exception e) {
      throw new SellOrderException(SellOrderErrorCode.UNKNOWN, e);
    }
  }
}
