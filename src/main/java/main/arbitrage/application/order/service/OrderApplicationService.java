package main.arbitrage.application.order.service;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.order.dto.OrderCalcResult;
import main.arbitrage.auth.security.SecurityUtil;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.buyOrder.service.BuyOrderService;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
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
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitAccountResponse;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitOrderResponse;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;
import main.arbitrage.presentation.dto.request.OrderRequest;
import main.arbitrage.presentation.dto.request.UpdateLeverageRequest;
import main.arbitrage.presentation.dto.request.UpdateMarginTypeRequest;
import main.arbitrage.presentation.dto.response.BuyOrderResponse;
import main.arbitrage.presentation.dto.response.OrderResponse;
import main.arbitrage.presentation.dto.view.UserTradeInfo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {
  private final BuyOrderService buyOrderService;
  private final SellOrderService sellOrderService;
  private final ExchangePrivateRestFactory exchangePrivateRestFactory;
  private final UserEnvService userEnvService;
  private final SymbolVariableService symbolVariableService;
  private final ExchangeRateService exchangeRateService;

  @Transactional
  public Map<Long, OrderResponse> createSellOrder(OrderRequest req) {
    ExchangeRate exchangeRate = exchangeRateService.getNonNullUsdToKrw();
    Symbol symbol = symbolVariableService.findSymbolByName(req.symbol());

    Long userId = SecurityUtil.getUserId();
    UserEnv userEnv = userEnvService.findAndExistByUserId(userId);

    User user = userEnv.getUser();

    ExchangePrivateRestPair upbitExchangePrivateRestPair =
        exchangePrivateRestFactory.create(userEnv);
    BinancePrivateRestService binanceService = upbitExchangePrivateRestPair.getBinance();
    UpbitPrivateRestService upbitService = upbitExchangePrivateRestPair.getUpbit();

    List<BuyOrder> openOrders = buyOrderService.getAndExistOpenOrders(user, symbol);

    List<OrderCalcResult> results = new ArrayList<>();
    BigDecimal qty = BigDecimal.valueOf(req.qty());

    double upbitTotalQty = sellOrderService.calculateSellQty(results, openOrders, qty);

    BinanceOrderResponse binanceOrderRes =
        binanceService.order( // 시장가 롱 ( 판매 )
            symbol.getName(), BinanceEnums.Side.BUY, BinanceEnums.Type.MARKET, req.qty(), null);

    String uuid =
        upbitService.order(
            symbol.getName(),
            UpbitOrderEnums.Side.ask,
            UpbitOrderEnums.OrdType.market,
            null,
            upbitTotalQty);

    UpbitOrderResponse upbitOrderRes = upbitService.order(uuid, 5);

    // buy order update와 sell order create
    for (OrderCalcResult orderCalcResult : results) {
      if (orderCalcResult.isClose()) {
        orderCalcResult.getBuyOrder().close();
      }

      sellOrderService.createMarketOrder(
          orderCalcResult, binanceOrderRes, upbitOrderRes, exchangeRate);
    }

    return results.stream()
        .map(result -> OrderResponse.fromEntity(result.getBuyOrder()))
        .collect(Collectors.toMap(OrderResponse::getId, order -> order));
  }

  @Transactional
  public BuyOrderResponse createBuyOrder(OrderRequest req) {
    ExchangeRate exchangeRate = exchangeRateService.getNonNullUsdToKrw();

    Symbol symbol = symbolVariableService.findSymbolByName(req.symbol());
    String symbolName = symbol.getName();

    Long userId = SecurityUtil.getUserId();

    UserEnv userEnv = userEnvService.findAndExistByUserId(userId);

    User user = userEnv.getUser();

    ExchangePrivateRestPair upbitExchangePrivateRestPair =
        exchangePrivateRestFactory.create(userEnv);
    BinancePrivateRestService binanceService = upbitExchangePrivateRestPair.getBinance();
    UpbitPrivateRestService upbitService = upbitExchangePrivateRestPair.getUpbit();

    BinanceOrderResponse binanceOrderRes =
        binanceService.order( // 시장가 숏
            symbolName, BinanceEnums.Side.SELL, BinanceEnums.Type.MARKET, req.qty(), null);

    String uuid =
        upbitService.order(
            symbolName,
            UpbitOrderEnums.Side.bid,
            UpbitOrderEnums.OrdType.price,
            Math.round(binanceOrderRes.cumQuote() * exchangeRate.getRate()),
            null);

    UpbitOrderResponse upbitOrderRes = upbitService.order(uuid, 5);

    // 주문 완료.
    OrderResponse orderResponse =
        OrderResponse.fromEntity(
            buyOrderService.createMarketBuyOrder(
                user, symbol, exchangeRate, binanceOrderRes, upbitOrderRes));

    // 지갑 조회 및 포지션 조회 시작
    List<UpbitAccountResponse> upbitAccount = upbitService.getAccount();

    // 업비트 전부 구매시 krw는 0.000...소수점 단위로 남아있기 때문에 반환값이 나옴.
    // 바이낸스는 0원이어도 반환값을 줌.
    double krw = Double.valueOf(upbitService.getKRW(upbitAccount).get().balance());
    double usdt = Double.valueOf(binanceService.getUSDT().get().balance());

    // 구매 성공시 포지션이 반드시 있으므로 그대로 반환
    Optional<UpbitAccountResponse> upbitOptionalCurrentSymbolInfo =
        upbitService.getCurrentSymbol(upbitAccount, symbolName);

    BinancePositionInfoResponse binancePositionInfo = binanceService.getPositionInfo(symbolName);

    return BuyOrderResponse.builder()
        .orderResponse(orderResponse)
        .upbitPosition(upbitOptionalCurrentSymbolInfo.get())
        .binancePosition(binancePositionInfo)
        .usdt(usdt)
        .krw(krw)
        .build();
  }

  @Transactional
  public UserTradeInfo getTradeInfo(String symbolName, Long userId) {
    Optional<UserEnv> userEnvOptional = userEnvService.findByUserId(userId);

    if (userEnvOptional.isEmpty()) return null;

    UserTradeInfo.UserTradeInfoBuilder tradeInfoBuilder = UserTradeInfo.builder();

    UserEnv userEnv = userEnvOptional.get();

    // 서비스를 만듬
    ExchangePrivateRestPair upbitExchangePrivateRestPair =
        exchangePrivateRestFactory.create(userEnv);
    BinancePrivateRestService binanceService = upbitExchangePrivateRestPair.getBinance();
    UpbitPrivateRestService upbitService = upbitExchangePrivateRestPair.getUpbit();

    // 업비트 account가져와 KRW가 존재하는지 검사
    List<UpbitAccountResponse> upbitAccount = upbitService.getAccount();
    Optional<UpbitAccountResponse> optionalKRW = upbitService.getKRW(upbitAccount);

    // 지갑정보 section
    if (optionalKRW.isPresent()) {
      // krw가 존재할 시 krw와 usdt를 build. USDT는 바이낸스측에서 값이 0이더라도 항상 줌.
      tradeInfoBuilder
          .krw(Double.valueOf(optionalKRW.get().balance()))
          .usdt(Double.valueOf(binanceService.getUSDT().get().balance()));
    }

    // 포지션 section
    Optional<UpbitAccountResponse> upbitOptionalCurrentSymbolInfo =
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
    List<OrderResponse> orders = new ArrayList<>();
    List<BuyOrder> buyOrders =
        buyOrderService.getOrders(
            userEnv.getUser(), symbolVariableService.findSymbolByName(symbolName));
    for (BuyOrder buyOrder : buyOrders) {
      orders.add(OrderResponse.fromEntity(buyOrder));
    }
    tradeInfoBuilder.orders(orders);

    return tradeInfoBuilder.build();
  }

  @Transactional
  public BinanceChangeLeverageResponse updateLeverage(UpdateLeverageRequest req) {
    Long userId = SecurityUtil.getUserId();

    UserEnv userEnv = userEnvService.findAndExistByUserId(userId);

    ExchangePrivateRestPair upbitExchangePrivateRestPair =
        exchangePrivateRestFactory.create(userEnv);
    BinancePrivateRestService binanceService = upbitExchangePrivateRestPair.getBinance();

    BinanceChangeLeverageResponse response =
        binanceService.changeLeverage(req.symbol(), req.leverage());

    return response;
  }

  @Transactional
  public boolean updateMarginType(UpdateMarginTypeRequest req) {
    Long userId = SecurityUtil.getUserId();

    UserEnv userEnv = userEnvService.findAndExistByUserId(userId);

    ExchangePrivateRestPair upbitExchangePrivateRestPair =
        exchangePrivateRestFactory.create(userEnv);
    BinancePrivateRestService binanceService = upbitExchangePrivateRestPair.getBinance();

    boolean response = binanceService.updateMarginType(req.symbol(), req.marginType());

    return response;
  }
}
