package main.arbitrage.domain.buyOrder.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.buyOrder.dto.BuyOrderResDto;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.buyOrder.repository.BuyOrderRepository;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.global.util.math.MathUtil;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.dto.order.BinanceOrderResponseDto;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order.UpbitGetOrderResponseDto;

@Service
@RequiredArgsConstructor
public class BuyOrderService {
    private final double BINANCE_TAKER_COMM = 0.0005d;
    private final BuyOrderRepository buyOrderRepository;

    public BuyOrderResDto createMarketBuyOrder(User user, Symbol symbol, ExchangeRate exchangeRate,
            BinanceOrderResponseDto binanceOrderRes, UpbitGetOrderResponseDto upbitOrderRes) {
        double binanceAvgPrice = binanceOrderRes.getAvgPrice(); // 바이낸스 평단가
        double binanceQty = binanceOrderRes.getExecutedQty(); // 바이낸스 체결 개수
        double binanceTotalPrice = binanceAvgPrice * binanceQty; // 바이낸스 숏에 사용한 총 USDT
        double binanceCommission = MathUtil.roundTo(binanceTotalPrice * BINANCE_TAKER_COMM, 8); // 바이낸스
                                                                                                // 수수료
        double upbitTotalPrice = upbitOrderRes.getPrice(); // 업비트 구매에 사용한 총 KRW
        double upbitQty = upbitOrderRes.getExecutedVolume(); // 업비트 구매된 개수
        double upbitAvgPrice = Math.round(upbitTotalPrice / upbitQty); // 업비트 평단가
        double upbitCommission = upbitOrderRes.getPaidFee(); // 업비트 수수료
        double usdToKrw = exchangeRate.getRate(); // 원달러 환율

        double premium = MathUtil.calculatePremium(upbitAvgPrice, binanceAvgPrice, usdToKrw);

        buyOrderRepository.save(BuyOrder.builder().user(user).symbol(symbol)
                .exchangeRate(exchangeRate).premium((float) premium).upbitPrice(upbitAvgPrice)
                .upbitQuantity((float) upbitQty).upbitCommission((float) upbitCommission)
                .binancePrice((float) binanceAvgPrice).binanceQuantity((float) binanceQty)
                .binanceCommission((float) binanceCommission).isMaker(false).isClose(false)
                .build());

        return BuyOrderResDto.builder().premium(premium).binanceAvgPrice(binanceAvgPrice)
                .binanceQty(binanceQty).binanceTotalPrice(binanceTotalPrice)
                .binanceCommission(binanceCommission).upbitTotalPrice(upbitTotalPrice)
                .upbitQty(upbitQty).upbitAvgPrice(upbitAvgPrice).upbitCommission(upbitCommission)
                .usdToKrw(usdToKrw).build();
    }
}
