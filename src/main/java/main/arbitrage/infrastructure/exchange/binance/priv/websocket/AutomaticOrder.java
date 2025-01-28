package main.arbitrage.infrastructure.exchange.binance.priv.websocket;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import main.arbitrage.application.auto.dto.AutoTradingStandardValueDTO;
import main.arbitrage.application.auto.dto.AutomaticUserInfoDTO;
import main.arbitrage.application.auto.dto.WalletDTO;
import main.arbitrage.application.collector.dto.PremiumDTO;
import main.arbitrage.application.order.dto.OrderCalcResultDTO;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.buyOrder.service.BuyOrderService;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
import main.arbitrage.domain.price.service.PriceService;
import main.arbitrage.domain.sellOrder.service.SellOrderService;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.global.util.math.MathUtil;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums;
import main.arbitrage.infrastructure.exchange.binance.dto.event.BinanceOrderTradeUpdateEvent;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceAccountResponse;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceExchangeInfoResponse;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceOrderResponse;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
import main.arbitrage.infrastructure.exchange.upbit.dto.enums.UpbitOrderEnums;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitAccountResponse;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitOrderResponse;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;

// 해당 class는 UserStream의 주문, 돈, Key 관리를 담당.
// 자동거래가 on일경우에만 쓰레드를 할당하여 자동거래가 돌아감
public class AutomaticOrder {
  protected ScheduledExecutorService executorService; // Thread(중요)
  private ScheduledFuture<?> scheduler; // Thread(중요)

  protected AutomaticUserInfoDTO automaticUser;
  protected BinanceExchangeInfoResponse exchangeInfo;

  private WalletDTO initialWallet;
  private WalletDTO currentWallet;
  private Symbol symbol;
  private float avgExchangeRate;
  private float targetProfitRate;
  private float kneeValue;
  private float shoulderValue;
  private float minimumProfitRate;
  private float minimumMovePrice;
  private int decimalPlaces;

  protected final SymbolVariableService symbolVariableService;

  private final BuyOrderService buyOrderService; // database
  private final SellOrderService sellOrderService; // database
  private final PriceService priceService; // database

  private final ExchangeRateService exchangeRateService; // 환율

  private final BinancePrivateRestService binanceService; // binance주문
  private final UpbitPrivateRestService upbitService; // upbit주문

  private final LinkedList<BuyOrder> openOrders = new LinkedList<>();
  private final Queue<Double> priceQueue = new LinkedBlockingDeque<>(5); // 최대 5
  private double prevBinancePrice;

  private boolean isLock = true;

  private static final int UPBIT_MINIMUM_PRICE = 5000;

  public AutomaticOrder(
      AutomaticUserInfoDTO automaticUser,
      BinanceExchangeInfoResponse exchangeInfo,
      SymbolVariableService symbolVariableService,
      BuyOrderService buyOrderService,
      SellOrderService sellOrderService,
      ExchangeRateService exchangeRateService,
      PriceService priceService,
      BinancePrivateRestService binanceService,
      UpbitPrivateRestService upbitService) {

    this.automaticUser = automaticUser;
    this.exchangeInfo = exchangeInfo;
    this.symbolVariableService = symbolVariableService;
    this.buyOrderService = buyOrderService;
    this.sellOrderService = sellOrderService;
    this.exchangeRateService = exchangeRateService;
    this.priceService = priceService;
    this.binanceService = binanceService;
    this.upbitService = upbitService;

    if (automaticUser.autoFlag()) {
      this.symbol = automaticUser.autoTradingStrategy().getSymbol();
      this.executorService = Executors.newSingleThreadScheduledExecutor();
      this.openOrders.addAll(buyOrderService.getOpenOrders(automaticUser.userId(), symbol));
      setStandardSchedule();
      updateWallet();
    }
  }

  public void run(PremiumDTO dto) {
    if (isLock) return; // 잠금임
    if (executorService.isShutdown()) return; // 쓰레드 없음
    if (!symbol.getName().equals(dto.getSymbol())) return; // 내가 지정한 심볼이 아님

    executorService.execute(
        () -> {
          double binancePrice = dto.getBinance();

          Double moveValue = getMoveValue(binancePrice);
          if (moveValue == null) return;

          float premiumOfStandardExchangeRate =
              MathUtil.calculatePremium(dto.getUpbit(), dto.getBinance(), avgExchangeRate);

          // 매도
          if (openOrders.size() > 0) {
            Optional<BuyOrder> buyOrderOptional =
                openOrders.stream()
                    .filter(
                        openOrder -> {
                          float premiumOfOrder =
                              MathUtil.calculatePremium(
                                  openOrder.getUpbitPrice(),
                                  openOrder.getBinancePrice(),
                                  avgExchangeRate);

                          // 익절
                          if (targetProfitRate < premiumOfStandardExchangeRate - premiumOfOrder)
                            return true;

                          // 손절
                          if (automaticUser.autoTradingStrategy().getStopLossPercent()
                              > premiumOfStandardExchangeRate - premiumOfOrder) return true;

                          return false;
                        })
                    .findFirst();

            // 익절, 손절중 하나
            if (buyOrderOptional.isPresent()) {
              // Thread에 계속 요청을 못보내도록 잠금
              lock();
              BuyOrder buyOrder = buyOrderOptional.get();

              double qty = buyOrder.getRestBinanceQty().doubleValue();

              double currentPrice =
                  MathUtil.roundTo(binancePrice - moveValue, decimalPlaces).doubleValue();

              buyBinance(symbol.getName(), qty, currentPrice);
            }
          }

          // 매수
          // 실시간으로 레버리지 계산을 하려면 테이블을 수정해야하네??!?!?!??!?!?!?!?!?아아아아아악
          if (kneeValue > premiumOfStandardExchangeRate) {}
        });
  }

  // 업비트 주문 관리
  protected void buyUpbit(BinanceOrderTradeUpdateEvent orderTradeUpdateEvent) {
    ExchangeRate exchangeRate = exchangeRateService.getNonNullUsdToKrw();
    Symbol symbol = orderTradeUpdateEvent.getSymbol();
    String uuid =
        upbitService.order(
            symbol.getName(),
            UpbitOrderEnums.Side.bid,
            UpbitOrderEnums.OrdType.price,
            Math.round(
                orderTradeUpdateEvent.getPrice()
                    * orderTradeUpdateEvent.getQuantity()
                    * exchangeRate.getRate()),
            null);

    UpbitOrderResponse upbitOrderRes = upbitService.order(uuid, 5);

    BuyOrder buyOrder =
        buyOrderService.createLimitOrder(
            automaticUser.userId(), symbol, exchangeRate, orderTradeUpdateEvent, upbitOrderRes);

    openOrders.addLast(buyOrder);
  }

  protected void sellUpbit(BinanceOrderTradeUpdateEvent orderTradeUpdateEvent) {
    Symbol symbol = orderTradeUpdateEvent.getSymbol();

    ExchangeRate exchangeRate = exchangeRateService.getNonNullUsdToKrw();
    List<OrderCalcResultDTO> results = new ArrayList<>();
    BigDecimal qty = BigDecimal.valueOf(orderTradeUpdateEvent.getQuantity());

    double upbitQty = sellOrderService.calculateSellQty(results, openOrders, qty);

    String uuid =
        upbitService.order(
            symbol.getName(),
            UpbitOrderEnums.Side.ask,
            UpbitOrderEnums.OrdType.market,
            null,
            upbitQty);

    UpbitOrderResponse upbitOrderRes = upbitService.order(uuid, 5);

    for (OrderCalcResultDTO orderCalcResult : results) {
      BuyOrder buyOrder = orderCalcResult.getBuyOrder();

      if (orderCalcResult.isClose()) {
        buyOrder.close();
        buyOrderService.updateBuyOrder(buyOrder); // buyOrder update
        openOrders.remove(buyOrder); // list동기화
      }
      buyOrder.addSellOrder(
          sellOrderService.createLimitOrder(
              orderCalcResult, orderTradeUpdateEvent, upbitOrderRes, exchangeRate));
    }
  }

  // 바이낸스 주문 관리
  private void sellBinance(String symbolName, double qty, double price) {
    binanceService.order(symbolName, BinanceEnums.Side.BUY, BinanceEnums.Type.LIMIT, qty, price);
  }

  private void buyBinance(String symbolName, double qty, double price) {
    binanceService.order(symbolName, BinanceEnums.Side.BUY, BinanceEnums.Type.LIMIT, qty, price);
  }

  protected BinanceOrderResponse cancelBinance(String symbolName, String clientId) {
    return binanceService.cancelOrder(symbolName, clientId);
  }

  // UserStream Key관리
  protected String createListenKey() {
    return binanceService.createListenKey();
  }

  public String updateListenKey() {
    return binanceService.updateListenKey();
  }

  // 지갑 관리
  protected void updateWallet() {
    BinanceAccountResponse binanceAccount = binanceService.getUSDT().get();
    UpbitAccountResponse upbitAccount = upbitService.getKRW().get();

    currentWallet.setKrw(Double.valueOf(upbitAccount.balance()));
    currentWallet.setUsdt(Double.valueOf(binanceAccount.balance()));

    updateInitialWallet();
  }

  private void updateInitialWallet() {
    // openOrder가 없을시 지금 잔액이 최초 잔액임
    if (openOrders.isEmpty()) {
      initialWallet.setKrw(currentWallet.getKrw());
      initialWallet.setUsdt(currentWallet.getUsdt());

      return;
    }

    // openOrder가 있으면 잔액 계산해서 최초 잔액 집어넣기
    double krw = currentWallet.getKrw();
    double usdt = currentWallet.getUsdt();

    for (BuyOrder openOrder : openOrders) {
      double binanceRestQty = openOrder.getRestBinanceQty().doubleValue();
      double upbitRestQty = openOrder.getRestUpbitQty().doubleValue();

      krw += openOrder.getUpbitPrice() * upbitRestQty;
      usdt += openOrder.getBinancePrice() * binanceRestQty;
    }

    initialWallet.setKrw(krw);
    initialWallet.setUsdt(usdt);
  }

  // Lock관리
  protected void lock() {
    isLock = true;
  }

  protected void unlock() {
    isLock = false;
  }

  private void setStandardSchedule() {
    // scheduler가 null이면 이전 scheduler를 종료하고 다시 set
    if (scheduler != null) scheduler.cancel(true);

    scheduler =
        executorService.scheduleAtFixedRate(
            () -> {
              AutoTradingStandardValueDTO standardValue =
                  priceService.getAutoTradingValue(
                      automaticUser.autoTradingStrategy().getSymbol(),
                      automaticUser.autoTradingStrategy().getEntryCandleMinutes());

              // 평균 환율
              avgExchangeRate = standardValue.avgExchangeRate();

              // 무릎 값
              kneeValue =
                  MathUtil.calculatePercentValue(
                      standardValue.minPremium(),
                      standardValue.maxPremium(),
                      automaticUser.autoTradingStrategy().getKneeEntryPercent());

              // 어깨 값
              shoulderValue =
                  MathUtil.calculatePercentValue(
                      standardValue.minPremium(),
                      standardValue.maxPremium(),
                      automaticUser.autoTradingStrategy().getShoulderEntryPercent());

              // 최소 수익률
              minimumProfitRate =
                  automaticUser.autoTradingStrategy().getMinimumProfitTargetPercent();

              // 목표 수익률
              targetProfitRate =
                  kneeValue - shoulderValue < minimumProfitRate
                      ? minimumProfitRate
                      : kneeValue - shoulderValue;

              // 최소 가격 움직임(소수점 자리수)
              decimalPlaces = MathUtil.getDecimalPlaces(exchangeInfo.stepSize());

              // 최소 가격 움직임
              minimumMovePrice =
                  MathUtil.roundTo(exchangeInfo.stepSize() * 3, decimalPlaces).floatValue();
            },
            0,
            automaticUser.autoTradingStrategy().getEntryCandleMinutes() / 2,
            TimeUnit.MINUTES);
  }

  private Double getMoveValue(double binancePrice) {
    priceQueue.offer(Math.abs(binancePrice - prevBinancePrice));
    prevBinancePrice = binancePrice;

    if (priceQueue.size() != 5) return null;
    double average = priceQueue.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    priceQueue.poll(); // avg구하고 queue 삭제

    return minimumMovePrice > average ? minimumMovePrice : average;
  }

  // 종료
  public void shutdown() {
    if (!executorService.isShutdown()) {
      executorService.shutdown();
    }
  }
}
