package main.arbitrage.domain.price.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "price")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long price_id;

    @Column(name = "symbol", length = 5)
    private String symbol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_rate_id", nullable = false)
    private ExchangeRate exchangeRate;

    @Column(name = "premium", nullable = false)
    private BigDecimal premium;

    @Column(name = "upbit", nullable = false)
    private BigDecimal upbit;

    @Column(name = "binance", nullable = false)
    private BigDecimal binance;

    @Column(name = "upbit_trade_at", nullable = false)
    private Timestamp upbitTradeAt;

    @Column(name = "binance_trade_at", nullable = false)
    private Timestamp binanceTradeAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP(0) WITHOUT TIME ZONE")
    private Timestamp createdAt;

    @Builder
    public Price(
            String symbol,
            ExchangeRate exchangeRate,
            BigDecimal premium,
            BigDecimal upbit,
            BigDecimal binance,
            Long upbitTradeAt,
            Long binanceTradeAt
    ) {
        this.symbol = symbol;
        this.exchangeRate = exchangeRate;
        this.premium = premium;
        this.upbit = upbit;
        this.binance = binance;
        this.upbitTradeAt = new Timestamp(upbitTradeAt);
        this.binanceTradeAt = new Timestamp(binanceTradeAt);
    }

    public Price withCreatedAt(Date createdAt) {
        this.createdAt = new Timestamp((createdAt.getTime() + 500) / 1000 * 1000); // 반올림 하여 초단위 저장
        return this;
    }
}