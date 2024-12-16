package main.arbitrage.domain.price.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.symbol.entity.Symbol;

import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "price")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symbol_id", nullable = false)
    private Symbol symbol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_rate_id", nullable = false)
    private ExchangeRate exchangeRate;

    @Column(name = "premium", nullable = false, columnDefinition = "REAL")
    private float premium;

    @Column(name = "upbit", nullable = false, columnDefinition = "DOUBLE PRECISION")
    private double upbit;

    @Column(name = "binance", nullable = false, columnDefinition = "REAL")
    private float binance;

    @Column(name = "upbit_trade_at", nullable = false, columnDefinition = "TIMESTAMP(6) WITHOUT TIME ZONE")
    private Timestamp upbitTradeAt;

    @Column(name = "binance_trade_at", nullable = false, columnDefinition = "TIMESTAMP(6) WITHOUT TIME ZONE")
    private Timestamp binanceTradeAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP(0) WITHOUT TIME ZONE")
    private Timestamp createdAt;

    /*
     * Timestamp의 괄호안의 숫자
     * 0 -> 초단위
     * 3 -> 밀리초
     * 6 -> 마이크로초
     * */

    @Builder
    public Price(
            Symbol symbol,
            ExchangeRate exchangeRate,
            double premium,
            double upbit,
            double binance,
            Long upbitTradeAt,
            Long binanceTradeAt
    ) {
        this.symbol = symbol;
        this.exchangeRate = exchangeRate;
        this.premium = (float) premium;
        this.upbit = upbit;
        this.binance = (float) binance;
        this.upbitTradeAt = new Timestamp(upbitTradeAt);
        this.binanceTradeAt = new Timestamp(binanceTradeAt);
    }

    public Price withCreatedAt(Date createdAt) {
        this.createdAt = new Timestamp((createdAt.getTime() + 500) / 1000 * 1000); // 반올림 하여 초단위 저장
        return this;
    }
}