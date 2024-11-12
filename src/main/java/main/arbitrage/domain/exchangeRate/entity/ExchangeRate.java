package main.arbitrage.domain.exchangeRate.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.arbitrage.domain.symbolPrice.entity.SymbolPrice;

@Entity
@Table(name = "exchange_rate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exchange_rate_id;

    @Column(name = "to_currency")
    private String toCurrency;

    @Column(name = "from_currency")
    private String fromCurrency;

    @Column(name = "rate")
    private double rate;

    @OneToOne(mappedBy = "exchangeRate", cascade = CascadeType.ALL, orphanRemoval = true)
    private SymbolPrice symbolPrice;

    @Builder
    public ExchangeRate(String fromCurrency, String toCurrency, double rate) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
    }
}