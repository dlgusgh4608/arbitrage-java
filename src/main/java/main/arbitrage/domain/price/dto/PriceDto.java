package main.arbitrage.domain.price.dto;

import java.sql.Timestamp;

import lombok.Getter;
import main.arbitrage.domain.price.entity.Price;


@Getter
public class PriceDto {
    private String symbol;
    private float premium;
    private double upbit;
    private float binance;
    private Timestamp upbitTradeAt;
    private Timestamp binanceTradeAt;
    private Timestamp createdAt;

    public static PriceDto from(Price price) {
        PriceDto dto = new PriceDto();
        dto.symbol = price.getSymbol().getName();
        dto.premium = price.getPremium();
        dto.upbit = price.getUpbit();
        dto.binance = price.getBinance();
        dto.upbitTradeAt = price.getUpbitTradeAt();
        dto.binanceTradeAt = price.getBinanceTradeAt();
        dto.createdAt = price.getCreatedAt();
        return dto;
    }
}