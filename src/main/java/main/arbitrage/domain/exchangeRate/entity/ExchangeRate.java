package main.arbitrage.domain.exchangeRate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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
   * 통화의 심볼은 대부분이 3자리더라. USD -> 유나이티드 스테이트, DOLLAR KRW -> 코리아, WON
   */

  @Column(name = "to_currency", nullable = false, columnDefinition = "CHAR(3)")
  private String toCurrency;

  @Column(name = "from_currency", nullable = false, columnDefinition = "CHAR(3)")
  private String fromCurrency;

  @Column(name = "rate", columnDefinition = "REAL")
  private float rate;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public ExchangeRate(String fromCurrency, String toCurrency, float rate) {
    this.fromCurrency = fromCurrency;
    this.toCurrency = toCurrency;
    this.rate = rate;
  }
}
