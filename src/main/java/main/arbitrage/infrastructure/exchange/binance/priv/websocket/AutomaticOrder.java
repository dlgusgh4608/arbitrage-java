package main.arbitrage.infrastructure.exchange.binance.priv.websocket;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
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
import main.arbitrage.global.util.regex.KeyGenUtil;
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
@Slf4j
public class AutomaticOrder {
  protected ScheduledExecutorService executorService; // Thread(중요)
  private ScheduledFuture<?> scheduler; // Thread(중요)

  private final WalletDTO initialWallet = new WalletDTO();
  private final WalletDTO currentWallet = new WalletDTO();

  AutoTradingStandardValueDTO standardValue;

  // 초기화 해야하는 아이들
  protected AutomaticUserInfoDTO automaticUser;
  private BinanceExchangeInfoResponse exchangeInfo;
  private Symbol symbol;
  private Integer leverage;
  private Float avgExchangeRate;
  private Float kneeValue;
  private Float shoulderValue;
  private Float fixedProfitRate;
  private Float minimumProfitRate;
  private Float minimumMovePrice;
  private Integer decimalPlacesOfStepSize;
  private Integer decimalPlacesOfTickSize;
  private final LinkedList<BuyOrder> openOrders = new LinkedList<>();
  private final Queue<Double> priceQueue = new LinkedBlockingDeque<>(5); // 최대 5
  private Double prevBinancePrice;

  protected final SymbolVariableService symbolVariableService;

  private final BuyOrderService buyOrderService; // database
  private final SellOrderService sellOrderService; // database
  private final PriceService priceService; // database

  private final ExchangeRateService exchangeRateService; // 환율

  private final BinancePrivateRestService binanceService; // binance주문
  private final UpbitPrivateRestService upbitService; // upbit주문

  private boolean isLock = true;

  private static final double SAFETY_WALLET_PERCENT = 0.95;

  public AutomaticOrder(
      AutomaticUserInfoDTO automaticUser,
      Map<String, BinanceExchangeInfoResponse> exchangeInfoMap,
      SymbolVariableService symbolVariableService,
      BuyOrderService buyOrderService,
      SellOrderService sellOrderService,
      ExchangeRateService exchangeRateService,
      PriceService priceService,
      BinancePrivateRestService binanceService,
      UpbitPrivateRestService upbitService) {

    this.automaticUser = automaticUser;
    this.symbolVariableService = symbolVariableService;
    this.buyOrderService = buyOrderService;
    this.sellOrderService = sellOrderService;
    this.exchangeRateService = exchangeRateService;
    this.priceService = priceService;
    this.binanceService = binanceService;
    this.upbitService = upbitService;

    if (automaticUser.autoFlag()) {
      this.symbol = automaticUser.tradingStrategy().getSymbol();
      this.exchangeInfo = exchangeInfoMap.get(symbol.getName());
      this.executorService = Executors.newSingleThreadScheduledExecutor();
      this.openOrders.addAll(buyOrderService.getOpenOrders(automaticUser.userId(), symbol));
      updateLeverage(automaticUser.tradingStrategy().getLeverage());
      setStandardSchedule();
      updateWallet();
    }
  }

  public void updateAutomaticValue(
      Map<String, BinanceExchangeInfoResponse> exchangeInfoMap,
      AutomaticUserInfoDTO automaticUser) {
    this.automaticUser = automaticUser;
    this.symbol = automaticUser.tradingStrategy().getSymbol();
    this.exchangeInfo = exchangeInfoMap.get(symbol.getName());
    if (executorService == null) {
      this.executorService = Executors.newSingleThreadScheduledExecutor();
    }
    this.openOrders.clear();
    this.openOrders.addAll(buyOrderService.getOpenOrders(automaticUser.userId(), symbol));
    updateLeverage(automaticUser.tradingStrategy().getLeverage());
    setStandardSchedule();
    updateWallet();
    unlock();
  }

  // todo: 로깅 추가.
  public void run(PremiumDTO dto) {
    if (isLock) return; // 잠금임
    if (executorService == null || executorService.isShutdown()) return; // 쓰레드 없음
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

                          double profitRate = premiumOfStandardExchangeRate - premiumOfOrder;

                          // 고정 익절값으로 익절
                          if (fixedProfitRate != 0.0 && fixedProfitRate < profitRate) return true;

                          // 유동 익절값으로 익절
                          if (fixedProfitRate == 0.0
                              && minimumProfitRate < profitRate
                              && shoulderValue < premiumOfStandardExchangeRate) return true;

                          // 손절
                          if (automaticUser.tradingStrategy().getStopLossPercent() > profitRate)
                            return true;

                          return false;
                        })
                    .findFirst();

            // 익절, 손절중 하나
            if (buyOrderOptional.isPresent()) {
              lock();
              BuyOrder buyOrder = buyOrderOptional.get();

              double qty = buyOrder.getRestBinanceQty().doubleValue();

              double targetPrice =
                  MathUtil.floorTo(binancePrice - moveValue, decimalPlacesOfTickSize).doubleValue();

              buyBinance(symbol.getName(), qty, targetPrice);
            }
          }

          // 매수
          if (kneeValue > premiumOfStandardExchangeRate) {
            double safeWalletPercent = SAFETY_WALLET_PERCENT - Math.abs(dto.getPremium()) / 100;

            double totalPriceOfWallet = calculateTotalPrice(safeWalletPercent, dto.getUsdToKrw());

            // 주문할 총액이 최소 주문금액보다 작은지 확인 업비트 5,000원이고 바이낸스는 보통 5,000원 보단 크다.
            if (totalPriceOfWallet < exchangeInfo.minUsdt()) return;

            double targetPrice =
                MathUtil.floorTo(binancePrice + moveValue, decimalPlacesOfTickSize).doubleValue();

            double targetQuantity =
                MathUtil.floorTo(totalPriceOfWallet / targetPrice, decimalPlacesOfStepSize)
                    .doubleValue();

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

                            // 추가매수가에 도달했는지 판단
                            if (automaticUser.tradingStrategy().getAdditionalBuyTargetPercent()
                                > premiumOfStandardExchangeRate - premiumOfOrder) return true;

                            return false;
                          })
                      .findFirst();

              if (buyOrderOptional.isPresent()) {
                lock();
                sellBinance(symbol.getName(), targetQuantity, targetPrice);
              }
            } else {
              lock();
              sellBinance(symbol.getName(), targetQuantity, targetPrice);
            }
          }
        });
  }

  private double calculateTotalPrice(double safeWalletPercent, float exchangeRate) {
    double initialKrw = initialWallet.getKrw();
    double initialUsdt = initialWallet.getUsdt();
    double currentKrw = currentWallet.getKrw();
    double currentUsdt = currentWallet.getUsdt();

    double totalPriceOfInitialWallet =
        Math.min(MathUtil.krwToUsd(initialKrw, exchangeRate), initialUsdt)
            * safeWalletPercent
            / automaticUser.tradingStrategy().getDivisionCount();

    double totalPriceOfCurrentWallet =
        Math.min(MathUtil.krwToUsd(currentKrw, exchangeRate), currentUsdt) * safeWalletPercent;

    return Math.min(totalPriceOfCurrentWallet, totalPriceOfInitialWallet);
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
    updateWallet();
    unlock();
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
    updateWallet();
    unlock();
  }

  // 바이낸스 주문 관리
  private void sellBinance(String symbolName, double qty, double price) {
    binanceService.order(
        KeyGenUtil.generate(),
        symbolName,
        BinanceEnums.Side.SELL,
        BinanceEnums.Type.LIMIT,
        qty,
        price);
  }

  private void buyBinance(String symbolName, double qty, double price) {
    binanceService.order(
        KeyGenUtil.generate(),
        symbolName,
        BinanceEnums.Side.BUY,
        BinanceEnums.Type.LIMIT,
        qty,
        price);
  }

  protected BinanceOrderResponse cancelBinance(String symbolName, String clientId) {
    try {
      return binanceService.cancelOrder(symbolName, clientId);
    } catch (Exception e) {
      log.error("이미 캔슬된 주문입니다.", e);
      return null;
    }
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
    currentWallet.setUsdt(Double.valueOf(binanceAccount.balance()) * leverage);

    updateInitialWallet();
  }

  private void updateInitialWallet() {
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

  // 스케줄 생성
  private void setStandardSchedule() {
    // scheduler가 null이면 이전 scheduler를 종료하고 다시 set
    if (scheduler != null) scheduler.cancel(true);

    scheduler =
        executorService.scheduleAtFixedRate(
            () -> {
              standardValue =
                  priceService.getAutoTradingValue(
                      automaticUser.tradingStrategy().getSymbol(),
                      automaticUser.tradingStrategy().getEntryCandleMinutes());

              // 평균 환율
              avgExchangeRate = standardValue.avgExchangeRate();

              // 무릎 값
              kneeValue =
                  MathUtil.calculatePercentValue(
                      standardValue.minPremium(),
                      standardValue.maxPremium(),
                      automaticUser.tradingStrategy().getKneeEntryPercent());

              // 어깨 값
              shoulderValue =
                  MathUtil.calculatePercentValue(
                      standardValue.minPremium(),
                      standardValue.maxPremium(),
                      automaticUser.tradingStrategy().getShoulderEntryPercent());

              // 고정 수익률 (0일 시 무시)
              fixedProfitRate = automaticUser.tradingStrategy().getFixedProfitTargetPercent();

              // 최소 수익률
              minimumProfitRate = automaticUser.tradingStrategy().getMinimumProfitTargetPercent();

              // 최소 주문 개수
              decimalPlacesOfStepSize = MathUtil.getDecimalPlaces(exchangeInfo.stepSize());

              // 최소 가격 움직임
              decimalPlacesOfTickSize = MathUtil.getDecimalPlaces(exchangeInfo.tickSize());

              // 최소 가격 움직임 * 3
              minimumMovePrice =
                  MathUtil.roundTo(exchangeInfo.tickSize() * 3, decimalPlacesOfTickSize)
                      .floatValue();
            },
            0,
            automaticUser.tradingStrategy().getEntryCandleMinutes() / 2,
            TimeUnit.MINUTES);
  }

  // 평균 이동값
  private Double getMoveValue(double binancePrice) {
    if (prevBinancePrice == null) {
      prevBinancePrice = binancePrice;
      return null;
    }

    priceQueue.offer(Math.abs(binancePrice - prevBinancePrice));
    prevBinancePrice = binancePrice;

    if (priceQueue.size() < 5) return null;
    double average = priceQueue.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    priceQueue.poll(); // avg구하고 queue 삭제

    return minimumMovePrice > average ? minimumMovePrice : average;
  }

  // 바이낸스 레버리지 관리
  private void updateLeverage(int leverage) {
    this.leverage = binanceService.changeLeverage(symbol.getName(), leverage).leverage();
  }

  // 종료
  public void shutdown() {
    lock();
    if (!executorService.isShutdown()) {
      executorService.shutdownNow();
      executorService = null;
      scheduler = null;
      automaticUser = null;
      exchangeInfo = null;
      symbol = null;
      avgExchangeRate = null;
      kneeValue = null;
      shoulderValue = null;
      minimumProfitRate = null;
      fixedProfitRate = null;
      minimumMovePrice = null;
      decimalPlacesOfStepSize = null;
      decimalPlacesOfTickSize = null;
      prevBinancePrice = null;
      openOrders.clear();
      priceQueue.clear();
      prevBinancePrice = null;
    }
  }
}
