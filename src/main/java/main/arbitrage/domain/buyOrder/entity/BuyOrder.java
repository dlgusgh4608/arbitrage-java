package main.arbitrage.domain.buyOrder.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.arbitrage.domain.buyOrder.dto.BuyOrderResDto;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.user.entity.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "buy_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BuyOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symbol_id", nullable = false)
    private Symbol symbol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_rate_id", nullable = false)
    private ExchangeRate exchangeRate;

    @Column(name = "premium", nullable = false, columnDefinition = "REAL")
    private float premium;

    @Column(name = "upbit_price", nullable = false, columnDefinition = "DOUBLE PRECISION")
    private double upbitPrice;

    @Column(name = "upbit_quantity", nullable = false, columnDefinition = "REAL")
    private float upbitQuantity;

    @Column(name = "upbit_commission", nullable = false, columnDefinition = "REAL")
    private float upbitCommission;

    @Column(name = "binance_price", nullable = false, columnDefinition = "REAL")
    private float binancePrice;

    @Column(name = "binance_quantity", nullable = false, columnDefinition = "REAL")
    private float binanceQuantity;

    @Column(name = "binance_commission", nullable = false, columnDefinition = "REAL")
    private float binanceCommission;

    @Column(name = "is_maker", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isMaker;

    @Column(name = "is_close", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isClose;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public BuyOrder(
        User user,
        Symbol symbol,
        ExchangeRate exchangeRate,
        float premium,
        double upbitPrice,
        float upbitQuantity,
        float upbitCommission,
        float binancePrice,
        float binanceQuantity,
        float binanceCommission,
        boolean isMaker,
        boolean isClose
    ) {
        this.user = user;
        this.symbol = symbol;
        this.exchangeRate = exchangeRate;
        this.premium = premium;
        this.upbitPrice = upbitPrice;
        this.upbitQuantity = upbitQuantity;
        this.upbitCommission = upbitCommission;
        this.binancePrice = binancePrice;
        this.binanceQuantity = binanceQuantity;
        this.binanceCommission = binanceCommission;
        this.isMaker = isMaker;
        this.isClose = isClose;
    }
}