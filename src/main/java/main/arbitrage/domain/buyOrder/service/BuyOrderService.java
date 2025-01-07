package main.arbitrage.domain.buyOrder.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.buyOrder.exception.BuyOrderErrorCode;
import main.arbitrage.domain.buyOrder.exception.BuyOrderException;
import main.arbitrage.domain.buyOrder.repository.BuyOrderRepository;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.global.util.math.MathUtil;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceOrderResponse;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitGetOrderResponse;
import main.arbitrage.presentation.dto.response.BuyOrderResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuyOrderService {
  private final double BINANCE_TAKER_COMM = 0.0005d;
  private final BuyOrderRepository buyOrderRepository;

  public BuyOrderResponse createMarketBuyOrder(
      User user,
      Symbol symbol,
      ExchangeRate exchangeRate,
      BinanceOrderResponse binanceOrderRes,
      UpbitGetOrderResponse upbitOrderRes) {
    try {
      double binanceAvgPrice = binanceOrderRes.avgPrice(); // 바이낸스 평단가
      double binanceQty = binanceOrderRes.executedQty(); // 바이낸스 체결 개수
      double binanceTotalPrice = binanceAvgPrice * binanceQty; // 바이낸스 숏에 사용한 총 USDT
      double binanceCommission =
          MathUtil.roundTo(binanceTotalPrice * BINANCE_TAKER_COMM, 8); // 바이낸스
      // 수수료
      double upbitTotalPrice = upbitOrderRes.price(); // 업비트 구매에 사용한 총 KRW
      double upbitQty = upbitOrderRes.executedVolume(); // 업비트 구매된 개수
      double upbitAvgPrice = Math.round(upbitTotalPrice / upbitQty); // 업비트 평단가
      double upbitCommission = upbitOrderRes.paidFee(); // 업비트 수수료
      double usdToKrw = exchangeRate.getRate(); // 원달러 환율

      double premium = MathUtil.calculatePremium(upbitAvgPrice, binanceAvgPrice, usdToKrw);

      BuyOrder buyOrder =
          buyOrderRepository.save(
              BuyOrder.builder()
                  .user(user)
                  .symbol(symbol)
                  .exchangeRate(exchangeRate)
                  .premium(premium)
                  .upbitPrice(upbitAvgPrice)
                  .upbitQuantity(upbitQty)
                  .upbitCommission(upbitCommission)
                  .binancePrice(binanceAvgPrice)
                  .binanceQuantity(binanceQty)
                  .binanceCommission(binanceCommission)
                  .isMaker(false)
                  .isClose(false)
                  .build());

      return BuyOrderResponse.fromEntity(buyOrder);
    } catch (Exception e) {
      String serverMessage =
          String.format(
              "매수 주문 생성 오류\nuserId: %d\nsymbolName: %s\nbinanceResponse: %s\n upbitReseponse: %s",
              user.getId(), symbol.getName(), binanceOrderRes, upbitOrderRes);

      throw new BuyOrderException(BuyOrderErrorCode.UNKNOWN, serverMessage, e);
    }
  }

  public List<BuyOrder> getOpenOrders(User user, Symbol symbol) {
    try {
      return buyOrderRepository.findByUserAndSymbolAndIsCloseFalse(user, symbol);
    } catch (Exception e) {
      throw new BuyOrderException(BuyOrderErrorCode.UNKNOWN, e);
    }
  }

  public List<BuyOrder> getAndExistOpenOrders(User user, Symbol symbol) {
    try {
      List<BuyOrder> buyOrders =
          buyOrderRepository.findByUserAndSymbolAndIsCloseFalse(user, symbol);

      if (buyOrders.isEmpty()) {
        throw new BuyOrderException(BuyOrderErrorCode.NOT_FOUND);
      }

      return buyOrders;
    } catch (BuyOrderException e) {
      throw e;
    } catch (Exception e) {
      throw new BuyOrderException(BuyOrderErrorCode.UNKNOWN, e);
    }
  }

  public List<BuyOrder> getOrders(User user, Symbol symbol) {
    try {
      return buyOrderRepository.findByUserAndSymbol(user, symbol);
    } catch (Exception e) {
      throw new BuyOrderException(BuyOrderErrorCode.UNKNOWN, e);
    }
  }
}
