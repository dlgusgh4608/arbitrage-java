package main.arbitrage.domain.exchangeRate.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.arbitrage.domain.price.entity.Price;

@Entity
@Table(name = "exchange_rate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exchange_rate_id;

    @Column(name = "to_currency", length = 3)
    private String toCurrency;

    @Column(name = "from_currency", length = 3)
    private String fromCurrency;

    @Column(name = "rate")
    private double rate;

    @Builder
    public ExchangeRate(String fromCurrency, String toCurrency, double rate) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
    }
}