package main.arbitrage.application.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.auth.security.SecurityUtil;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.buyOrder.service.BuyOrderService;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
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

@Service
@RequiredArgsConstructor
public class OrderApplicationService {
    private final BuyOrderService buyOrderService;
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
        if (exchangeRate == null)
            throw new Exception("Exchange rate is not found");

        Symbol symbol = symbolVariableService.findSymbolByName(req.symbol());
        String orderType = req.orderType();

        if (symbol == null)
            throw new IllegalArgumentException("Symbol is not found");

        if (!orderType.equals(OrderType.BUY.name()) && !orderType.equals(OrderType.SELL.name()))
            throw new IllegalArgumentException("Invalid order type");


        ExchangeRate fixedExchangeRate = exchangeRate;

        Long userId = SecurityUtil.getUserId();
        Optional<UserEnv> userEnvOptional = userEnvService.findByUserId(userId);

        if (userEnvOptional.isEmpty())
            throw new IllegalArgumentException("UserEnv is not found");

        UserEnv userEnv = userEnvOptional.get();
        User user = userEnv.getUser();

        ExchangePrivateRestPair upbitExchangePrivateRestPair =
                exchangePrivateRestFactory.create(userEnv);
        BinancePrivateRestService binanceService = upbitExchangePrivateRestPair.getBinance();
        UpbitPrivateRestService upbitService = upbitExchangePrivateRestPair.getUpbit();

        if (orderType.equals(OrderType.BUY.name())) {
            BinanceOrderResponse binanceOrderRes = binanceService.order( // 시장가 숏
                    symbol.getName(), BinanceEnums.Side.SELL, BinanceEnums.Type.MARKET, req.qty(),
                    null);

            String uuid = upbitService.order(symbol.getName(), UpbitOrderEnums.Side.bid,
                    UpbitOrderEnums.OrdType.price,
                    (double) Math.round(binanceOrderRes.cumQuote() * fixedExchangeRate.getRate()),
                    null);

            UpbitGetOrderResponse upbitOrderRes = upbitService.order(uuid, 5);

            // if(upbitOrderRes == null) {} // upbit order의 정보를 가져오는데 실패했을경우 바이낸스도 신속하게 팔아치우기

            return buyOrderService.createMarketBuyOrder(user, symbol, fixedExchangeRate,
                    binanceOrderRes, upbitOrderRes);
        } else {
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
            tradeInfoBuilder.krw(Double.valueOf(optionalKRW.get().balance()))
                    .usdt(Double.valueOf(binanceService.getUSDT().get().balance()));
        }

        // 포지션 section
        Optional<UpbitGetAccountResponse> upbitOptionalCurrentSymbolInfo =
                upbitService.getCurrentSymbol(upbitAccount, symbolName);

        BinancePositionInfoResponse binancePositionInfo =
                binanceService.getPositionInfo(symbolName);

        if (upbitOptionalCurrentSymbolInfo.isPresent() && binancePositionInfo != null) {
            tradeInfoBuilder.upbitPosition(upbitOptionalCurrentSymbolInfo.get())
                    .binancePosition(binancePositionInfo);
        }

        // 심볼 정보 (현재 마진 타입, 레버리지)
        BinanceSymbolInfoResponse symbolInfo = binanceService.symbolInfo(symbolName);
        tradeInfoBuilder.marginType(symbolInfo.marginType()).leverage(symbolInfo.leverage());

        // 레버리지 한도
        tradeInfoBuilder.brackets(binanceService.getLeverageBrackets(symbolName).brackets());


        // 주문 history
        List<BuyOrderResponse> orders = new ArrayList<>();
        List<BuyOrder> buyOrders = buyOrderService.getOrders(userEnv.getUser(),
                symbolVariableService.findSymbolByName(symbolName));
        for (BuyOrder buyOrder : buyOrders) {
            orders.add(BuyOrderResponse.fromEntity(buyOrder));
        }
        tradeInfoBuilder.orders(orders);

        return tradeInfoBuilder.build();
    }


    @Transactional
    public BinanceChangeLeverageResponse updateLeverage(UpdateLeverageRequest req)
            throws Exception {
        Long userId = SecurityUtil.getUserId();

        Optional<UserEnv> userEnvOptional = userEnvService.findByUserId(userId);

        if (userEnvOptional.isEmpty())
            throw new IllegalArgumentException("UserEnv is not found");

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

        if (userEnvOptional.isEmpty())
            throw new IllegalArgumentException("UserEnv is not found");

        UserEnv userEnv = userEnvOptional.get();

        ExchangePrivateRestPair upbitExchangePrivateRestPair =
                exchangePrivateRestFactory.create(userEnv);
        BinancePrivateRestService binanceService = upbitExchangePrivateRestPair.getBinance();

        boolean response = binanceService.updateMarginType(req.symbol(), req.marginType());

        return response;
    }
}
