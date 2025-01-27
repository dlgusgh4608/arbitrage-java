package main.arbitrage.application.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.order.dto.ExchangeMarketPositionDTO;
import main.arbitrage.application.order.dto.OrderCalcResultDTO;
import main.arbitrage.auth.security.SecurityUtil;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.buyOrder.service.BuyOrderService;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
import main.arbitrage.domain.sellOrder.service.SellOrderService;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
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
import main.arbitrage.presentation.dto.response.SellOrderResponse;
import main.arbitrage.presentation.dto.view.UserTradeInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  public SellOrderResponse createSellOrder(OrderRequest req) {
    ExchangeRate exchangeRate = exchangeRateService.getNonNullUsdToKrw();
    Symbol symbol = symbolVariableService.findAndExistSymbolByName(req.symbol());
    String symbolName = symbol.getName();

    Long userId = SecurityUtil.getUserId();
    UserEnv userEnv = userEnvService.findAndExistByUserId(userId);

    ExchangePrivateRestPair upbitExchangePrivateRestPair =
        exchangePrivateRestFactory.create(userEnv);
    BinancePrivateRestService binanceService = upbitExchangePrivateRestPair.getBinance();
    UpbitPrivateRestService upbitService = upbitExchangePrivateRestPair.getUpbit();

    List<BuyOrder> openOrders = buyOrderService.getAndExistOpenOrders(userId, symbol);

    List<OrderCalcResultDTO> results = new ArrayList<>();
    BigDecimal qty = BigDecimal.valueOf(req.qty());

    double upbitTotalQty = sellOrderService.calculateSellQty(results, openOrders, qty);

    BinanceOrderResponse binanceOrderRes =
        binanceService.order(
            symbolName, BinanceEnums.Side.BUY, BinanceEnums.Type.MARKET, req.qty(), null);

    String uuid =
        upbitService.order(
            symbol.getName(),
            UpbitOrderEnums.Side.ask,
            UpbitOrderEnums.OrdType.market,
            null,
            upbitTotalQty);

    UpbitOrderResponse upbitOrderRes = upbitService.order(uuid, 5);

    // buy order update와 sell order create
    for (OrderCalcResultDTO orderCalcResult : results) {
      if (orderCalcResult.isClose()) {
        orderCalcResult.getBuyOrder().close();
      }

      sellOrderService.createMarketOrder(
          orderCalcResult, binanceOrderRes, upbitOrderRes, exchangeRate);
    }

    // 지갑, 포지션 정보 가져옴
    ExchangeMarketPositionDTO exchangeMarketPosition =
        getExchangeMarketPosition(binanceService, upbitService, symbolName);

    return SellOrderResponse.builder()
        .orderResponse(
            results.stream()
                .map(result -> OrderResponse.fromEntity(result.getBuyOrder()))
                .collect(Collectors.toMap(OrderResponse::getId, order -> order)))
        .krw(exchangeMarketPosition.getKrw())
        .usdt(exchangeMarketPosition.getUsdt())
        .upbitPosition(exchangeMarketPosition.getUpbitPosition())
        .binancePosition(exchangeMarketPosition.getBinancePosition())
        .build();
  }

  @Transactional
  public BuyOrderResponse createBuyOrder(OrderRequest req) {
    ExchangeRate exchangeRate = exchangeRateService.getNonNullUsdToKrw();

    Symbol symbol = symbolVariableService.findAndExistSymbolByName(req.symbol());
    String symbolName = symbol.getName();

    Long userId = SecurityUtil.getUserId();

    UserEnv userEnv = userEnvService.findAndExistByUserId(userId);

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
                userId, symbol, exchangeRate, binanceOrderRes, upbitOrderRes));

    // 지갑, 포지션 정보 가져옴
    ExchangeMarketPositionDTO exchangeMarketPosition =
        getExchangeMarketPosition(binanceService, upbitService, symbolName);

    return BuyOrderResponse.builder()
        .orderResponse(orderResponse)
        .krw(exchangeMarketPosition.getKrw())
        .usdt(exchangeMarketPosition.getUsdt())
        .upbitPosition(exchangeMarketPosition.getUpbitPosition())
        .binancePosition(exchangeMarketPosition.getBinancePosition())
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

    // 지갑 및 포지션 정보
    ExchangeMarketPositionDTO exchangeMarketPosition =
        getExchangeMarketPosition(binanceService, upbitService, symbolName);

    tradeInfoBuilder
        .krw(exchangeMarketPosition.getKrw())
        .usdt(exchangeMarketPosition.getUsdt())
        .upbitPosition(exchangeMarketPosition.getUpbitPosition())
        .binancePosition(exchangeMarketPosition.getBinancePosition());

    // 심볼 정보 (현재 마진 타입, 레버리지)
    BinanceSymbolInfoResponse symbolInfo = binanceService.symbolInfo(symbolName);
    tradeInfoBuilder.marginType(symbolInfo.marginType()).leverage(symbolInfo.leverage());

    // 레버리지 한도
    tradeInfoBuilder.brackets(binanceService.getLeverageBrackets(symbolName).brackets());

    // 주문 history
    List<OrderResponse> orders = new ArrayList<>();
    List<BuyOrder> buyOrders =
        buyOrderService.getOrderBySymbol(
            userId, symbolVariableService.findAndExistSymbolByName(symbolName), 0);

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

  @Transactional
  public List<OrderResponse> getOrderBySymbol(String symbolName, int page) {
    Long userId = SecurityUtil.getUserId();
    Symbol symbol = symbolVariableService.findAndExistSymbolByName(symbolName);
    return buyOrderService.getOrderBySymbol(userId, symbol, page).stream()
        .map(OrderResponse::fromEntity)
        .toList();
  }

  private ExchangeMarketPositionDTO getExchangeMarketPosition(
      BinancePrivateRestService binanceService,
      UpbitPrivateRestService upbitService,
      String symbolName) {

    // 빌더 선언
    ExchangeMarketPositionDTO.ExchangeMarketPositionDTOBuilder builder =
        ExchangeMarketPositionDTO.builder();

    // 지갑정보
    List<UpbitAccountResponse> upbitAccount = upbitService.getAccount();
    Optional<UpbitAccountResponse> optionalKRW = upbitService.getKRW(upbitAccount);

    // krw를 기준으로 조건 USDT는 바이낸스측에서 값이 0이더라도 항상 줌.
    if (optionalKRW.isPresent()) {
      builder
          .krw(Double.valueOf(optionalKRW.get().balance()))
          .usdt(Double.valueOf(binanceService.getUSDT().get().balance()));
    } else {
      builder.krw(0.0d).usdt(Double.valueOf(binanceService.getUSDT().get().balance()));
    }

    // 포지션 정보
    Optional<UpbitAccountResponse> upbitOptionalCurrentSymbolInfo =
        upbitService.getCurrentSymbol(upbitAccount, symbolName);

    BinancePositionInfoResponse binancePositionInfo = binanceService.getPositionInfo(symbolName);

    if (upbitOptionalCurrentSymbolInfo.isPresent()) {
      builder
          .upbitPosition(upbitOptionalCurrentSymbolInfo.get())
          .binancePosition(binancePositionInfo);
    } else {
      builder.upbitPosition(null).binancePosition(binancePositionInfo);
    }

    return builder.build();
  }
}
