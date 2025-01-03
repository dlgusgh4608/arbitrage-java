package main.arbitrage.presentation.dto.view;

import java.sql.Timestamp;
import lombok.Builder;
import lombok.Getter;
import main.arbitrage.domain.price.entity.Price;


@Getter
@Builder
public class PriceView {
    private final String symbol;
    private final double premium;
    private final double upbit;
    private final double binance;
    private final Timestamp upbitTradeAt;
    private final Timestamp binanceTradeAt;
    private final Timestamp createdAt;

    public static PriceView fromEntity(Price price) {
        return PriceView.builder().symbol(price.getSymbol().getName()).premium(price.getPremium())
                .upbit(price.getUpbit()).binance(price.getBinance())
                .upbitTradeAt(price.getUpbitTradeAt()).binanceTradeAt(price.getBinanceTradeAt())
                .createdAt(price.getCreatedAt()).build();
    }
}
