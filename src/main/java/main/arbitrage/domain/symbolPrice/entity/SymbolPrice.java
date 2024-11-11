package main.arbitrage.domain.symbolPrice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.arbitrage.domain.symbol.entity.Symbol;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "symbol_price")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SymbolPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symbol_id", nullable = false)
    private Symbol symbol_id;

    @Column(name = "premium", nullable = false)
    private BigDecimal premium;

    @Column(name = "domestic", nullable = false)
    private BigDecimal domestic;

    @Column(name = "overseas", nullable = false)
    private BigDecimal overseas;

    @Column(name = "usd_to_krw", nullable = false)
    private BigDecimal usdToKrw;

    @Column(name = "domestic_trade_at", nullable = false)
    private Timestamp domesticTradeAt;

    @Column(name = "overseas_trade_at", nullable = false)
    private Timestamp overseasTradeAt;
}