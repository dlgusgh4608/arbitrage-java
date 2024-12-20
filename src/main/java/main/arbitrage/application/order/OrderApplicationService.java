package main.arbitrage.application.order;

import java.util.Optional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.auth.security.SecurityUtil;
import main.arbitrage.domain.buyOrder.dto.BuyOrderReqDto;
import main.arbitrage.domain.buyOrder.dto.BuyOrderResDto;
import main.arbitrage.domain.buyOrder.service.BuyOrderService;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.userEnv.entity.UserEnv;
import main.arbitrage.domain.userEnv.service.UserEnvService;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.dto.order.BinanceOrderEnum;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.dto.order.BinanceOrderResponseDto;
import main.arbitrage.infrastructure.exchange.factory.ExchangePrivateRestFactory;
import main.arbitrage.infrastructure.exchange.factory.dto.ExchangePrivateRestPair;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order.UpbitGetOrderResponseDto;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order.UpbitOrderEnum;

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
    public BuyOrderResDto createBuyOrder(BuyOrderReqDto req) throws Exception {
        if (exchangeRate == null)
            throw new Exception("Exchange rate is not found");

        Symbol symbol = symbolVariableService.findSymbolByName(req.getSymbol());

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

        BinanceOrderResponseDto binanceOrderRes = binanceService.order( // 시장가 숏
                symbol.getName(), BinanceOrderEnum.Side.SELL, BinanceOrderEnum.Type.MARKET,
                req.getQty(), null);

        String uuid = upbitService.order(symbol.getName(), UpbitOrderEnum.Side.bid,
                UpbitOrderEnum.OrdType.price,
                (double) Math.round(binanceOrderRes.getCumQuote() * fixedExchangeRate.getRate()),
                null);

        UpbitGetOrderResponseDto upbitOrderRes = upbitService.order(uuid, 5);

        // if(upbitOrderRes == null) {} // upbit order의 정보를 가져오는데 실패했을경우 바이낸스도 신속하게 팔아치우기

        return buyOrderService.createMarketBuyOrder(user, symbol, fixedExchangeRate,
                binanceOrderRes, upbitOrderRes);
    }
}
