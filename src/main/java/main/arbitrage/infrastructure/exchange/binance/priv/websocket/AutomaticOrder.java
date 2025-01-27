package main.arbitrage.infrastructure.exchange.binance.priv.websocket;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import main.arbitrage.application.auto.dto.AutoTradingStandardValueDTO;
import main.arbitrage.application.auto.dto.AutomaticUserInfoDTO;
import main.arbitrage.application.order.dto.OrderCalcResultDTO;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.buyOrder.service.BuyOrderService;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
import main.arbitrage.domain.price.service.PriceService;
import main.arbitrage.domain.sellOrder.service.SellOrderService;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums;
import main.arbitrage.infrastructure.exchange.binance.dto.event.BinanceOrderTradeUpdateEvent;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceAccountResponse;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceOrderResponse;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
import main.arbitrage.infrastructure.exchange.upbit.dto.enums.UpbitOrderEnums;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitAccountResponse;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitOrderResponse;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;

// 해당 class는 UserStream의 주문, 돈, Key 관리를 담당.
// 자동거래가 on일경우에만 쓰레드를 할당하여 자동거래가 돌아가고 off라면 주문 및 지갑상태만 트래킹
public class AutomaticOrder {
  protected ScheduledExecutorService executorService; // Thread(중요)
  private ScheduledFuture<?> scheduler; // Thread(중요)
  protected AutomaticUserInfoDTO automaticUser;
  private AutoTradingStandardValueDTO standardValue;

  protected final SymbolVariableService symbolVariableService;

  private final BuyOrderService buyOrderService; // database
  private final SellOrderService sellOrderService; // database
  private final PriceService priceService; // database

  private final ExchangeRateService exchangeRateService; // 환율

  private final BinancePrivateRestService binanceService; // binance주문
  private final UpbitPrivateRestService upbitService; // upbit주문

  private final LinkedList<BuyOrder> openOrders = new LinkedList<>();

  private boolean isLock = true;

  public AutomaticOrder(
      AutomaticUserInfoDTO automaticUser,
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
      this.executorService = Executors.newSingleThreadScheduledExecutor();
      this.openOrders.addAll(
          buyOrderService.getOpenOrders(
              automaticUser.userId(), automaticUser.autoTradingStrategy().getSymbol()));
      setStandardSchedule();
    }
  }

  public void run(String symbolName) {
    if (isLock) return;
    if (executorService.isShutdown()) return;
    if (!automaticUser.autoTradingStrategy().getSymbol().getName().equals(symbolName)) return;

    executorService.execute(
        () -> {
          System.out.println(openOrders.size());
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

  protected BinanceOrderResponse cancelBinance(String symbol, String clientId) {
    return binanceService.cancelOrder(symbol, clientId);
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
    Optional<BinanceAccountResponse> binanceAccount = binanceService.getUSDT();
    Optional<UpbitAccountResponse> upbitAccount = upbitService.getKRW();
  }

  // Lock관리
  protected void lock() {
    this.isLock = true;
  }

  protected void unlock() {
    this.isLock = false;
  }

  private void setStandardSchedule() {
    scheduler =
        executorService.scheduleAtFixedRate(
            () -> {
              standardValue =
                  priceService.getAutoTradingValue(
                      automaticUser.autoTradingStrategy().getSymbol(),
                      automaticUser.autoTradingStrategy().getEntryCandleMinutes());
            },
            0,
            automaticUser.autoTradingStrategy().getEntryCandleMinutes() / 2,
            TimeUnit.MINUTES);
  }

  // 종료
  public void shutdown() {
    if (!executorService.isShutdown()) {
      executorService.shutdown();
    }
  }
}
