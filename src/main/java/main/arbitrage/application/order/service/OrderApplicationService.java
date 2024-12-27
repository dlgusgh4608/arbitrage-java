package main.arbitrage.application.order.service;

import java.util.Optional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.auth.security.SecurityUtil;
import main.arbitrage.domain.buyOrder.service.BuyOrderService;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.userEnv.entity.UserEnv;
import main.arbitrage.domain.userEnv.service.UserEnvService;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceLeverageBracketResponse;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceOrderResponse;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceSymbolInfoResponse;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
import main.arbitrage.infrastructure.exchange.dto.ExchangePrivateRestPair;
import main.arbitrage.infrastructure.exchange.factory.ExchangePrivateRestFactory;
import main.arbitrage.infrastructure.exchange.upbit.dto.enums.UpbitOrderEnums;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitGetOrderResponse;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;
import main.arbitrage.presentation.dto.request.BuyOrderRequest;
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
    public BuyOrderResponse createBuyOrder(BuyOrderRequest req) throws Exception {
        if (exchangeRate == null)
            throw new Exception("Exchange rate is not found");

        Symbol symbol = symbolVariableService.findSymbolByName(req.symbol());

        if (symbol == null)
            throw new IllegalArgumentException("Symbol is not found");

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
    }

    @Transactional
    public UserTradeInfo getSymbolInfo(String symbolName, Long userId) throws Exception {
        Optional<UserEnv> userEnvOptional = userEnvService.findByUserId(userId);

        if (userEnvOptional.isEmpty()) {
            return null;
        }

        UserEnv userEnv = userEnvOptional.get();

        ExchangePrivateRestPair upbitExchangePrivateRestPair =
                exchangePrivateRestFactory.create(userEnv);
        BinancePrivateRestService binanceService = upbitExchangePrivateRestPair.getBinance();
        UpbitPrivateRestService upbitService = upbitExchangePrivateRestPair.getUpbit();

        String usdt = binanceService.getUSDT().get().balance();
        String krw = upbitService.getKRW().get().balance();
        BinanceSymbolInfoResponse symbolInfo = binanceService.symbolInfo(symbolName);

        BinanceLeverageBracketResponse binanceLeverageBracketResponse =
                binanceService.getLeverageBrackets(symbolName);

        return UserTradeInfo.builder().krw(Double.valueOf(krw)).usdt(Double.valueOf(usdt))
                .marginType(symbolInfo.marginType()).leverage(symbolInfo.leverage())
                .brackets(binanceLeverageBracketResponse.brackets()).build();
    }
}
