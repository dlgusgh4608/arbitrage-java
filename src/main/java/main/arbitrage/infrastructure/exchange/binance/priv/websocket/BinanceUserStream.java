package main.arbitrage.infrastructure.exchange.binance.priv.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.application.auto.dto.AutomaticUserInfoDTO;
import main.arbitrage.domain.buyOrder.service.BuyOrderService;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
import main.arbitrage.domain.price.service.PriceService;
import main.arbitrage.domain.sellOrder.service.SellOrderService;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.global.util.math.MathUtil;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.ExecutionType;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.OrderType;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Side;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Status;
import main.arbitrage.infrastructure.exchange.binance.dto.event.BinanceOrderTradeUpdateEvent;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceExchangeInfoResponse;
import main.arbitrage.infrastructure.exchange.dto.ExchangePrivateRestPair;
import main.arbitrage.infrastructure.websocket.common.WebSocketClient;
import main.arbitrage.infrastructure.websocket.handler.BinanceUserStreamHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Slf4j
public class BinanceUserStream extends AutomaticOrder implements WebSocketClient {
  private static final String BASE_URL = "wss://fstream.binance.com/ws/";
  private final String socketName;
  private final BinanceUserStreamHandler binanceUserStreamHandler;

  // binance order의 정보를 종합 계산하기 위한 Map
  private final Map<String, List<BinanceOrderTradeUpdateEvent>> orderMap =
      new ConcurrentHashMap<>();

  // binance order의 Ticker를 관리하기 위한 Map
  private final Map<String, ScheduledFuture<?>> orderTickerMap = new ConcurrentHashMap<>();

  // Ticker의 Timer ms
  private static final long ORDER_TICKER_TIMEOUT = 3000; // 3sec

  private boolean isRunning = false;
  private WebSocketSession session;

  public BinanceUserStream(
      AutomaticUserInfoDTO automaticUser,
      BinanceExchangeInfoResponse exchangeInfo,
      BinanceUserStreamHandler binanceUserStreamHandler,
      ExchangePrivateRestPair exchangePrivateServicePair,
      SymbolVariableService symbolVariableService,
      BuyOrderService buyOrderService,
      SellOrderService sellOrderService,
      ExchangeRateService exchangeRateService,
      PriceService priceService) {
    super(
        automaticUser,
        exchangeInfo,
        symbolVariableService,
        buyOrderService,
        sellOrderService,
        exchangeRateService,
        priceService,
        exchangePrivateServicePair.getBinance(),
        exchangePrivateServicePair.getUpbit());
    this.socketName = "UserStream[" + automaticUser.userId() + "]";
    this.binanceUserStreamHandler = binanceUserStreamHandler;
  }

  @Override
  public void connect() {
    try {
      if (isRunning) throw new IllegalStateException(socketName + "UserStream is already running!");

      StandardWebSocketClient client = new StandardWebSocketClient();

      String listenKey = createListenKey();

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
      // 자동거래가 활성화 되어있을시 unLock
      if (automaticUser.autoFlag() == true) unlock();
    } catch (Exception e) {
      log.error("알 수 없는 에러입니다.", e);
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
    // eventType을 enum으로 하기엔 사용하지 않는 eventType이 많이 존재하기에 String으로 처리
    String eventType = data.get("e").asText();

    switch (eventType) {
      case "ACCOUNT_UPDATE" -> {
        break;
      }
      case "ORDER_TRADE_UPDATE" -> {
        JsonNode order = data.get("o");
        switchOrderTrade(
            BinanceOrderTradeUpdateEvent.builder()
                .eventTime(data.get("E").asLong())
                .clientId(order.get("c").asText())
                .orderType(order.get("o").asText())
                .executionType(order.get("x").asText())
                .status(order.get("X").asText())
                .side(order.get("S").asText())
                .symbol(
                    symbolVariableService.findSymbolByName(
                        order.get("s").asText().replace("USDT", "")))
                .price(order.get("L").asDouble())
                .quantity(order.get("l").asDouble())
                .isMaker(order.get("m").asBoolean())
                .commission(order.get("n").floatValue())
                .build());
        break;
      }
      case "listenKeyExpired" -> {
        break;
      }
      default -> {
        break;
      }
    }
  }

  // 청산될시 이벤트 (실제로 청산 당해봄)
  // BinanceOrderTradeUpdateEvent(eventTime=1737618483293, clientId=autoclose-1737618483209916950,
  // orderType=LIQUIDATION, executionType=NEW, status=NEW, side=BUY, symbol=ETH, price=0.0,
  // quantity=0.0, isMaker=false, commission=0.0)

  // BinanceOrderTradeUpdateEvent(eventTime=1737618483293, clientId=autoclose-1737618483209916950,
  // orderType=LIQUIDATION, executionType=TRADE, status=FILLED, side=BUY, symbol=ETH, price=3228.75,
  // quantity=0.009, isMaker=false, commission=0.0)
  private boolean validateOrder(BinanceOrderTradeUpdateEvent payload) {
    // 지원 심볼 여부
    if (payload.getSymbol() == null) return false;

    // 취소 주문 확인
    if (payload.getExecutionType().equals(ExecutionType.CANCELED.name())) return false;

    // 오더 타입이 청산인지 확인
    if (payload.getOrderType().equals(OrderType.LIQUIDATION.name())) return true;

    return true;
    // 해당 거래가 자동거래인지 확안
    // return KeyGenUtil.validate(payload.getClientId());
  }

  private void switchOrderTrade(BinanceOrderTradeUpdateEvent orderTradeUpdateEvent) {
    if (!validateOrder(orderTradeUpdateEvent)) return;

    try {
      Status status = Status.valueOf(orderTradeUpdateEvent.getStatus());

      switch (status) {
        case NEW -> {
          newOrderTrade(orderTradeUpdateEvent);
          break;
        }
        case PARTIALLY_FILLED -> {
          partiallyFilledOrderTrade(orderTradeUpdateEvent);
          break;
        }
        case FILLED -> {
          filledOrderTrade(orderTradeUpdateEvent);
          break;
        }
        default -> {
          break;
        }
      }
    } catch (Exception e) {
      log.error("invalid Status\tstatus: {}", orderTradeUpdateEvent.getStatus(), e);
    }
  }

  // 새 주문
  private void newOrderTrade(BinanceOrderTradeUpdateEvent payload) {
    String clientId = payload.getClientId();
    orderMap.put(clientId, new ArrayList<>());
    setTicker(payload.getSymbol().getName(), clientId);
  }

  // 부분 체결
  private void partiallyFilledOrderTrade(BinanceOrderTradeUpdateEvent payload) {
    String clientId = payload.getClientId();
    List<BinanceOrderTradeUpdateEvent> oldOrder = orderMap.get(clientId);

    // 동시성 문제로 binance cancel을 하는 중에 체결이 발생함.
    if (oldOrder == null) {
      List<BinanceOrderTradeUpdateEvent> newOrder = new ArrayList<>();
      newOrder.add(payload);
      orderMap.put(clientId, newOrder);
      setTicker(payload.getSymbol().getName(), clientId);
      return;
    }

    // oldOrder가 null이 아님으로 List를 반환받았으니
    // 정상적인 거래 flow로 add
    oldOrder.add(payload);
  }

  // 완전 체결
  private void filledOrderTrade(BinanceOrderTradeUpdateEvent payload) {
    String clientId = payload.getClientId();
    List<BinanceOrderTradeUpdateEvent> oldOrder = orderMap.get(clientId);

    // 타이머 종료 및 Map에서 데이터 삭제
    orderMap.remove(clientId);
    orderTickerMap.remove(clientId);
    ScheduledFuture<?> currentTicker = orderTickerMap.get(clientId);
    if (currentTicker != null) currentTicker.cancel(false);

    // 한번에 체결되지 않았으며, Ticker(ORDER_TICKER_TIMEOUT)의 시간이 경과하지 않았지만
    // 주문이 전부 체결되었을때 실행
    if (oldOrder != null && oldOrder.size() != 0) {

      oldOrder.add(payload);

      BinanceOrderTradeUpdateEvent calculatedPayload = calculateOrderTrade(oldOrder);

      orderForSide(calculatedPayload); // 진행
      return;
    }

    // cancel을 하는 도중 주문이 들어와 동시성 문제가 발생했을때
    // 주문이 한번에 다 체결된 경우
    orderForSide(payload); // 진행
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
        .orderType(firstOrderTrade.getOrderType())
        .status(firstOrderTrade.getStatus())
        .side(firstOrderTrade.getSide())
        .symbol(firstOrderTrade.getSymbol())
        .price(MathUtil.roundTo(sumPrice / orderTradeLength, 8).doubleValue())
        .quantity(MathUtil.roundTo(sumQty, 8).doubleValue())
        .isMaker(firstOrderTrade.getIsMaker())
        .commission(MathUtil.roundTo(sumCommission, 8).floatValue())
        .build();
  }

  // 해당 TIcker는 BinanceUserStream과 같은 Thread를 사용합니다.
  private void setTicker(String symbolName, String clientId) {
    if (executorService == null) return;

    // orderMap은 newOrderTrade와 partiallyFilledOrderTrade method에서 생성하며
    // 이는 동시성 문제를 해결하기 위함입니다.
    ScheduledFuture<?> ticker =
        executorService.schedule(
            () -> {
              try {
                Thread.sleep(ORDER_TICKER_TIMEOUT); // 3sec
                List<BinanceOrderTradeUpdateEvent> orders = orderMap.get(clientId);
                orderMap.remove(clientId);
                orderTickerMap.remove(clientId);

                // null이면 이미 완전체결 이후라 취소 할 필요 없음.
                if (orders != null) {
                  try {
                    cancelBinance(symbolName, clientId);
                  } catch (Exception e) {
                    log.error("이미 캔슬된 주문입니다.", e);
                  }

                  // null이 아니고 isEmpty가 false, 즉 체결된게 하나라도 있으면 거래 진행
                  if (!orders.isEmpty()) {
                    BinanceOrderTradeUpdateEvent payload = calculateOrderTrade(orders);
                    orderForSide(payload);
                  }
                }
              } catch (Exception e) {
                log.error(
                    "[ {}, {} ]: Ticker thread error",
                    Thread.currentThread().getName(),
                    socketName,
                    e);
              }
            },
            0,
            TimeUnit.MILLISECONDS);

    orderTickerMap.put(clientId, ticker);
  }
}
