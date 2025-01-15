package main.arbitrage.infrastructure.exchange.binance.priv.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.application.order.dto.OrderCalcResultDTO;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.buyOrder.service.BuyOrderService;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
import main.arbitrage.domain.sellOrder.service.SellOrderService;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.global.util.math.MathUtil;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.EventType;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Side;
import main.arbitrage.infrastructure.exchange.binance.dto.event.BinanceOrderTradeUpdateEvent;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
import main.arbitrage.infrastructure.exchange.dto.ExchangePrivateRestPair;
import main.arbitrage.infrastructure.exchange.upbit.dto.enums.UpbitOrderEnums;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitOrderResponse;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;
import main.arbitrage.infrastructure.websocket.common.WebSocketClient;
import main.arbitrage.infrastructure.websocket.handler.BinanceUserStreamHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

/*
 * Long userId
 * Symbol Entity -> 거래하려하는 Symbol을 가져와야함.
 * ExchangeRate Entity
 * 우선 임의의 값으로 넘겨받아 처리한 후 아래 Entity 생성하기.
 * ----
 * UserAutomatic Entity 생성.
 *  userId(Long)
 *  symbol(Symbol),
 *  stopLoss(Integer),
 *  targetProfitRate(integer),
 *  stairs(integer)[분할]
 *  additionalTarget(integer)[추가매수 타겟]
 */

@Slf4j
public class BinanceUserStream implements WebSocketClient {
  private static final String BASE_URL = "wss://fstream.binance.com/ws/";
  private final Long userId;
  private final Symbol symbol;
  private final String socketName;
  private final BinancePrivateRestService binanceService;
  private final UpbitPrivateRestService upbitService;
  private final BinanceUserStreamHandler binanceUserStreamHandler;
  private final BuyOrderService buyOrderService;
  private final SellOrderService sellOrderService;
  private final ExchangeRateService exchangeRateService;

  private final Map<String, List<BinanceOrderTradeUpdateEvent>> orderMap =
      new ConcurrentHashMap<>();
  private final Map<String, ScheduledFuture<?>> orderTickerMap = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private static final long ORDER_TICKER_TIMEOUT = 1500; // 1.5sec

  private boolean isRunning = false;
  private WebSocketSession session;

  public BinanceUserStream(
      Long userId, // 추후에 UserAutomatic로 변경
      Symbol symbol, // 추후에 UserAutomatic로 변경
      BinanceUserStreamHandler binanceUserStreamHandler,
      ExchangePrivateRestPair exchangePrivateServicePair,
      BuyOrderService buyOrderService,
      SellOrderService sellOrderService,
      ExchangeRateService exchangeRateService) {
    this.userId = userId;
    this.symbol = symbol;
    this.socketName = "UserStream[" + userId + "]";
    this.binanceUserStreamHandler = binanceUserStreamHandler;
    this.binanceService = exchangePrivateServicePair.getBinance();
    this.upbitService = exchangePrivateServicePair.getUpbit();
    this.buyOrderService = buyOrderService;
    this.sellOrderService = sellOrderService;
    this.exchangeRateService = exchangeRateService;
  }

  @Override
  public void connect() {
    try {
      if (isRunning) throw new IllegalStateException(socketName + "UserStream is already running!");

      StandardWebSocketClient client = new StandardWebSocketClient();

      String listenKey = binanceService.createListenKey();

      String websocketURL = BASE_URL.concat(listenKey);
      session =
          client
              .execute(
                  binanceUserStreamHandler
                      .setSocketName(socketName)
                      .setMessageHandler(this::handleMessage),
                  websocketURL)
              .get();

      isRunning = true;
    } catch (Exception e) {
      System.out.println(e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void disconnect() {
    if (session != null && session.isOpen()) {
      try {
        session.close();
        isRunning = false;
        log.info("{}\tBinance UserStream Disconnected", socketName);
      } catch (IOException e) {
        log.error("{}\tError closing Binance UserStream", socketName, e);
      }
    }
  }

  @Override
  public boolean isConnected() {
    return session != null && session.isOpen() && isRunning;
  }

  private void handleMessage(JsonNode data) {
    try {
      EventType eventType = EventType.valueOf(data.get("e").asText());

      switch (eventType) {
        case ACCOUNT_UPDATE -> {
          break;
        }
        case ORDER_TRADE_UPDATE -> {
          JsonNode order = data.get("o");

          switchOrderTrade(
              BinanceOrderTradeUpdateEvent.builder()
                  .eventTime(data.get("E").asLong())
                  .clientId(order.get("c").asText())
                  .status(order.get("X").asText())
                  .side(order.get("S").asText())
                  .symbol(order.get("s").asText())
                  .price(order.get("L").asDouble())
                  .quantity(order.get("l").asDouble())
                  .isMaker(order.get("m").asBoolean())
                  .commission(order.get("n").floatValue())
                  .build());
          break;
        }
        case listenKeyExpired -> {
          break;
        }
        default -> {
          break;
        }
      }
    } catch (Exception e) {
      System.out.println("invalid event type" + data.get("e").asText());
    }
  }

  private void switchOrderTrade(BinanceOrderTradeUpdateEvent orderTradeUpdateEvent) {
    switch (orderTradeUpdateEvent.getStatus()) {
      case "FILLED" -> filledOrderTrade(orderTradeUpdateEvent.getClientId(), orderTradeUpdateEvent);
      case "PARTIALLY_FILLED" ->
          partiallyFilledOrderTrade(orderTradeUpdateEvent.getClientId(), orderTradeUpdateEvent);
      default -> {
        break;
      }
    }
  }

  // 완전 체결
  private void filledOrderTrade(String clientId, BinanceOrderTradeUpdateEvent payload) {
    List<BinanceOrderTradeUpdateEvent> oldOrder = orderMap.get(clientId);

    // 이전 주문이 있으면 이전 부분체결 주문에서 걸린 Timer를 제거하고 계산후 진행.
    // 이전 주문이 없으면 그냥 진행.
    if (oldOrder != null) {
      ScheduledFuture<?> currentTicker = orderTickerMap.get(clientId);

      if (currentTicker != null) currentTicker.cancel(false);

      oldOrder.add(payload);

      BinanceOrderTradeUpdateEvent calculatedPayload = calculateOrderTrade(oldOrder);

      orderMap.remove(clientId);
      orderTickerMap.remove(clientId);

      orderForSide(calculatedPayload); // 진행
    } else {
      orderForSide(payload); // 진행
    }
  }

  // 부분 체결
  private void partiallyFilledOrderTrade(String clientId, BinanceOrderTradeUpdateEvent payload) {
    List<BinanceOrderTradeUpdateEvent> oldOrder = orderMap.get(clientId);

    // 이전 주문이 있으면 Map in List에 add
    // 이전 주문이 없으면 List add, Map put, setTicker(ORDER_TICKER_TIMEOUT)
    if (oldOrder != null) {
      oldOrder.add(payload);
    } else {
      List<BinanceOrderTradeUpdateEvent> newOrder = new ArrayList<>();
      newOrder.add(payload);
      orderMap.put(clientId, newOrder);
      setTicker(clientId);
    }
  }

  @Transactional
  private void sellUpbit(BinanceOrderTradeUpdateEvent orderTradeUpdateEvent) {
    List<BuyOrder> openOrders = buyOrderService.getAndExistOpenOrders(userId, symbol);
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
      if (orderCalcResult.isClose()) {
        orderCalcResult.getBuyOrder().close();
      }
      sellOrderService.createLimitOrder(
          orderCalcResult, orderTradeUpdateEvent, upbitOrderRes, exchangeRate);
    }
  }

  @Transactional
  private void buyUpbit(BinanceOrderTradeUpdateEvent orderTradeUpdateEvent) {
    ExchangeRate exchangeRate = exchangeRateService.getNonNullUsdToKrw();
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

    buyOrderService.createLimitOrder(
        userId, symbol, exchangeRate, orderTradeUpdateEvent, upbitOrderRes);
  }

  private void orderForSide(BinanceOrderTradeUpdateEvent orderTradeUpdateEvent) {
    if (orderTradeUpdateEvent.getSide().equals(Side.BUY.name())) {
      sellUpbit(orderTradeUpdateEvent);
    } else {
      buyUpbit(orderTradeUpdateEvent);
    }
  }

  // 주문 계산
  private BinanceOrderTradeUpdateEvent calculateOrderTrade(
      List<BinanceOrderTradeUpdateEvent> orderTrades) {
    BinanceOrderTradeUpdateEvent firstOrderTrade = orderTrades.get(0);
    int orderTradeLength = orderTrades.size();

    double sumQty =
        orderTrades.stream().mapToDouble(BinanceOrderTradeUpdateEvent::getQuantity).sum();
    double sumPrice =
        orderTrades.stream().mapToDouble(BinanceOrderTradeUpdateEvent::getPrice).sum();
    double sumCommission =
        orderTrades.stream().mapToDouble(BinanceOrderTradeUpdateEvent::getCommission).sum();

    return BinanceOrderTradeUpdateEvent.builder()
        .clientId(firstOrderTrade.getClientId())
        .status(firstOrderTrade.getStatus())
        .side(firstOrderTrade.getSide())
        .symbol(firstOrderTrade.getSymbol())
        .price(MathUtil.roundTo(sumPrice / orderTradeLength, 8).doubleValue())
        .quantity(MathUtil.roundTo(sumQty, 8).doubleValue())
        .isMaker(firstOrderTrade.getIsMaker())
        .commission(MathUtil.roundTo(sumCommission, 8).floatValue())
        .build();
  }

  // 주문 타이머
  private void setTicker(String clientId) {
    ScheduledFuture<?> ticker =
        scheduler.schedule(
            () -> {
              List<BinanceOrderTradeUpdateEvent> orders = orderMap.get(clientId);
              if (orders != null) {
                BinanceOrderTradeUpdateEvent payload = calculateOrderTrade(orders);

                orderMap.remove(clientId);
                orderTickerMap.remove(clientId);

                orderForSide(payload);
              }
            },
            ORDER_TICKER_TIMEOUT,
            TimeUnit.MICROSECONDS);

    orderTickerMap.put(clientId, ticker);
  }
}
