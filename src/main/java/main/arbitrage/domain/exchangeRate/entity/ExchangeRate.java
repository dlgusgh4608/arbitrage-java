package main.arbitrage.domain.exchangeRate.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.arbitrage.domain.price.entity.Price;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /*
     * 통화의 심볼은 대부분이 3자리더라.
     * USD -> 유나이티드 스테이트, DOLLAR
     * KRW -> 코리아, WON
     * */

    @Column(name = "to_currency", nullable = false, columnDefinition = "CHAR(3)")
    private String toCurrency;

    @Column(name = "from_currency", nullable = false, columnDefinition = "CHAR(3)")
    private String fromCurrency;

    @Column(name = "rate", columnDefinition = "REAL")
    private double rate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ExchangeRate(String fromCurrency, String toCurrency, double rate) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
    }
}