package main.arbitrage.domain.sellOrder.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.order.dto.OrderCalcResult;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.sellOrder.entity.SellOrder;
import main.arbitrage.domain.sellOrder.repository.SellOrderRepository;
import main.arbitrage.global.util.math.MathUtil;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceOrderResponse;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitGetOrderResponse;

@Service
@RequiredArgsConstructor
public class SellOrderService {
    private final double BINANCE_TAKER_COMM_RATE = 0.0005d;
    private final double UPBIT_COMM_RATE = 0.0005d;
    private final SellOrderRepository sellOrderRepository;

    public SellOrder createMarketOrder(OrderCalcResult orderCalcResult,
            BinanceOrderResponse binanceOrderResponse, UpbitGetOrderResponse upbitGetOrderResponse,
            ExchangeRate exchangeRate) {
        double binanceAvgPrice = binanceOrderResponse.avgPrice(); // 바이낸스 평단가
        double binanceQty = orderCalcResult.getBinanceQty().doubleValue(); // 바이낸스 체결 개수
        double binanceTotalPrice = binanceAvgPrice * binanceQty; // 바이낸스에 사용한 USDT
        double binanceCommission = MathUtil.roundTo(binanceTotalPrice * BINANCE_TAKER_COMM_RATE, 8);

        double upbitTotalPriceFromOutside = upbitGetOrderResponse.trades().stream()
                .mapToDouble(UpbitGetOrderResponse.Trade::funds).sum();
        double upbitQtyFromOutside = upbitGetOrderResponse.executedVolume();
        double upbitAvgPrice = Math.round(upbitTotalPriceFromOutside / upbitQtyFromOutside);

        double upbitQty = orderCalcResult.getUpbitQty().doubleValue();
        double upbitTotalPrice = upbitAvgPrice * upbitQty;
        double upbitCommission = MathUtil.roundTo(upbitTotalPrice * UPBIT_COMM_RATE, 8);
        float usdToKrw = exchangeRate.getRate(); // 원달러 환율

        double premium = MathUtil.calculatePremium(upbitAvgPrice, binanceAvgPrice, usdToKrw);
        float exchangeRateAtBuy = orderCalcResult.getBuyOrder().getExchangeRate().getRate();
        float premiumInBuyOrder = orderCalcResult.getBuyOrder().getPremium();
        double premiumWithBuyExchangeRate =
                MathUtil.calculatePremium(upbitAvgPrice, binanceAvgPrice, exchangeRateAtBuy);

        float profitRate = (float) MathUtil
                .roundTo((double) premiumInBuyOrder - premiumWithBuyExchangeRate, 4);


        SellOrder sellOrder = SellOrder.builder().buyOrder(orderCalcResult.getBuyOrder())
                .exchangeRate(exchangeRate).premium((float) premium).upbitPrice(upbitAvgPrice)
                .upbitQuantity((float) upbitQty).upbitCommission((float) upbitCommission)
                .binancePrice((float) binanceAvgPrice).binanceQuantity((float) binanceQty)
                .binanceCommission((float) binanceCommission).isMaker(false).profitRate(profitRate)
                .build();

        return sellOrderRepository.save(sellOrder);
    }
}
