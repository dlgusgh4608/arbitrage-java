package main.arbitrage.application.order.service;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.order.dto.OrderCalcResult;
import main.arbitrage.auth.security.SecurityUtil;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.buyOrder.service.BuyOrderService;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.sellOrder.service.SellOrderService;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.userEnv.entity.UserEnv;
import main.arbitrage.domain.userEnv.service.UserEnvService;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceChangeLeverageResponse;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceOrderResponse;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinancePositionInfoResponse;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceSymbolInfoResponse;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
import main.arbitrage.infrastructure.exchange.dto.ExchangePrivateRestPair;
import main.arbitrage.infrastructure.exchange.factory.ExchangePrivateRestFactory;
import main.arbitrage.infrastructure.exchange.upbit.dto.enums.UpbitOrderEnums;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitGetAccountResponse;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitGetOrderResponse;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;
import main.arbitrage.presentation.dto.enums.OrderType;
import main.arbitrage.presentation.dto.request.OrderRequest;
import main.arbitrage.presentation.dto.request.UpdateLeverageRequest;
import main.arbitrage.presentation.dto.request.UpdateMarginTypeRequest;
import main.arbitrage.presentation.dto.response.BuyOrderResponse;
import main.arbitrage.presentation.dto.view.UserTradeInfo;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {
  private final BuyOrderService buyOrderService;
  private final SellOrderService sellOrderService;
  private final ExchangePrivateRestFactory exchangePrivateRestFactory;
  private final UserEnvService userEnvService;
  private final SymbolVariableService symbolVariableService;

  private ExchangeRate exchangeRate;

  @EventListener
  public void customExchangeRate(ExchangeRate rate) {
    exchangeRate = rate;
  }

  @Transactional
  public BuyOrderResponse createOrder(OrderRequest req) throws Exception {
    if (exchangeRate == null) throw new Exception("Exchange rate is not found");

    Symbol symbol = symbolVariableService.findSymbolByName(req.symbol());
    String orderType = req.orderType();

    if (symbol == null) throw new IllegalArgumentException("Symbol is not found");

    if (!orderType.equals(OrderType.BUY.name()) && !orderType.equals(OrderType.SELL.name()))
      throw new IllegalArgumentException("Invalid order type");

    ExchangeRate fixedExchangeRate = exchangeRate;

    Long userId = SecurityUtil.getUserId();
    Optional<UserEnv> userEnvOptional = userEnvService.findByUserId(userId);

    if (userEnvOptional.isEmpty()) throw new IllegalArgumentException("UserEnv is not found");

    UserEnv userEnv = userEnvOptional.get();
    User user = userEnv.getUser();

    ExchangePrivateRestPair upbitExchangePrivateRestPair =
        exchangePrivateRestFactory.create(userEnv);
    BinancePrivateRestService binanceService = upbitExchangePrivateRestPair.getBinance();
    UpbitPrivateRestService upbitService = upbitExchangePrivateRestPair.getUpbit();

    // 구매 로직
    if (orderType.equals(OrderType.BUY.name())) {
      BinanceOrderResponse binanceOrderRes =
          binanceService.order( // 시장가 숏
              symbol.getName(), BinanceEnums.Side.SELL, BinanceEnums.Type.MARKET, req.qty(), null);

      String uuid =
          upbitService.order(
              symbol.getName(),
              UpbitOrderEnums.Side.bid,
              UpbitOrderEnums.OrdType.price,
              (double) Math.round(binanceOrderRes.cumQuote() * fixedExchangeRate.getRate()),
              null);

      UpbitGetOrderResponse upbitOrderRes = upbitService.order(uuid, 5);

      return buyOrderService.createMarketBuyOrder(
          user, symbol, fixedExchangeRate, binanceOrderRes, upbitOrderRes);
    } else { // 판매 로직
      BigDecimal qty = BigDecimal.valueOf(req.qty());

      List<BuyOrder> openOrders = buyOrderService.getOpenOrders(user, symbol);

      List<OrderCalcResult> results = new ArrayList<>();
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

      // qty가 음수이면 에러 발생
      if (qty.signum() < 0) throw new Exception("qty to larger");

      BinanceOrderResponse binanceOrderRes =
          binanceService.order( // 시장가 롱 ( 판매 )
              symbol.getName(), BinanceEnums.Side.BUY, BinanceEnums.Type.MARKET, req.qty(), null);

      String uuid =
          upbitService.order(
              symbol.getName(),
              UpbitOrderEnums.Side.ask,
              UpbitOrderEnums.OrdType.market,
              null,
              upbitTotalQty.doubleValue());

      UpbitGetOrderResponse upbitOrderRes = upbitService.order(uuid, 5);

      for (OrderCalcResult orderCalcResult : results) {
        if (orderCalcResult.isClose()) {
          orderCalcResult.getBuyOrder().close();
        }

        sellOrderService.createMarketOrder(
            orderCalcResult, binanceOrderRes, upbitOrderRes, fixedExchangeRate);
      }

      List<BuyOrderResponse> z =
          results.stream().map(r -> BuyOrderResponse.fromEntity(r.getBuyOrder())).toList();

      // 여기서부터 이어 작성해야함.
      for (BuyOrderResponse r : z) {
        System.out.println(r);
      }

      return null;
    }
  }

  @Transactional
  public UserTradeInfo getTradeInfo(String symbolName, Long userId) throws Exception {
    Optional<UserEnv> userEnvOptional = userEnvService.findByUserId(userId);

    if (userEnvOptional.isEmpty()) {
      return null;
    }

    UserTradeInfo.UserTradeInfoBuilder tradeInfoBuilder = UserTradeInfo.builder();

    UserEnv userEnv = userEnvOptional.get();

    // 서비스를 만듬
    ExchangePrivateRestPair upbitExchangePrivateRestPair =
        exchangePrivateRestFactory.create(userEnv);
    BinancePrivateRestService binanceService = upbitExchangePrivateRestPair.getBinance();
    UpbitPrivateRestService upbitService = upbitExchangePrivateRestPair.getUpbit();

    // 업비트 account가져와 KRW가 존재하는지 검사
    List<UpbitGetAccountResponse> upbitAccount = upbitService.getAccount();
    Optional<UpbitGetAccountResponse> optionalKRW = upbitService.getKRW(upbitAccount);

    // 지갑정보 section
    if (optionalKRW.isPresent()) {
      // krw가 존재할 시 krw와 usdt를 build. USDT는 바이낸스측에서 값이 0이더라도 항상 줌.
      tradeInfoBuilder
          .krw(Double.valueOf(optionalKRW.get().balance()))
          .usdt(Double.valueOf(binanceService.getUSDT().get().balance()));
    }

    // 포지션 section
    Optional<UpbitGetAccountResponse> upbitOptionalCurrentSymbolInfo =
        upbitService.getCurrentSymbol(upbitAccount, symbolName);

    BinancePositionInfoResponse binancePositionInfo = binanceService.getPositionInfo(symbolName);

    if (upbitOptionalCurrentSymbolInfo.isPresent() && binancePositionInfo != null) {
      tradeInfoBuilder
          .upbitPosition(upbitOptionalCurrentSymbolInfo.get())
          .binancePosition(binancePositionInfo);
    }

    // 심볼 정보 (현재 마진 타입, 레버리지)
    BinanceSymbolInfoResponse symbolInfo = binanceService.symbolInfo(symbolName);
    tradeInfoBuilder.marginType(symbolInfo.marginType()).leverage(symbolInfo.leverage());

    // 레버리지 한도
    tradeInfoBuilder.brackets(binanceService.getLeverageBrackets(symbolName).brackets());

    // 주문 history
    List<BuyOrderResponse> orders = new ArrayList<>();
    List<BuyOrder> buyOrders =
        buyOrderService.getOrders(
            userEnv.getUser(), symbolVariableService.findSymbolByName(symbolName));
    for (BuyOrder buyOrder : buyOrders) {
      orders.add(BuyOrderResponse.fromEntity(buyOrder));
    }
    tradeInfoBuilder.orders(orders);

    return tradeInfoBuilder.build();
  }

  @Transactional
  public BinanceChangeLeverageResponse updateLeverage(UpdateLeverageRequest req) throws Exception {
    Long userId = SecurityUtil.getUserId();

    Optional<UserEnv> userEnvOptional = userEnvService.findByUserId(userId);

    if (userEnvOptional.isEmpty()) throw new IllegalArgumentException("UserEnv is not found");

    UserEnv userEnv = userEnvOptional.get();

    ExchangePrivateRestPair upbitExchangePrivateRestPair =
        exchangePrivateRestFactory.create(userEnv);
    BinancePrivateRestService binanceService = upbitExchangePrivateRestPair.getBinance();

    BinanceChangeLeverageResponse response =
        binanceService.changeLeverage(req.symbol(), req.leverage());

    return response;
  }

  @Transactional
  public boolean updateMarginType(UpdateMarginTypeRequest req) throws Exception {
    Long userId = SecurityUtil.getUserId();

    Optional<UserEnv> userEnvOptional = userEnvService.findByUserId(userId);

    if (userEnvOptional.isEmpty()) throw new IllegalArgumentException("UserEnv is not found");

    UserEnv userEnv = userEnvOptional.get();

    ExchangePrivateRestPair upbitExchangePrivateRestPair =
        exchangePrivateRestFactory.create(userEnv);
    BinancePrivateRestService binanceService = upbitExchangePrivateRestPair.getBinance();

    boolean response = binanceService.updateMarginType(req.symbol(), req.marginType());

    return response;
  }
}
