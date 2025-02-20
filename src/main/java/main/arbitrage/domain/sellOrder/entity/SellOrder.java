package main.arbitrage.domain.sellOrder.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "sell_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "buy_order_id", nullable = false)
  private BuyOrder buyOrder;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exchange_rate_id", nullable = false)
  private ExchangeRate exchangeRate;

  @Column(name = "premium", nullable = false, columnDefinition = "REAL")
  private float premium;

  @Column(name = "upbit_price", nullable = false, columnDefinition = "DOUBLE PRECISION")
  private double upbitPrice;

  @Column(name = "upbit_quantity", nullable = false, columnDefinition = "DOUBLE PRECISION")
  private double upbitQuantity;

  @Column(name = "upbit_commission", nullable = false, columnDefinition = "REAL")
  private float upbitCommission;

  @Column(
      name = "upbit_event_time",
      nullable = false,
      columnDefinition = "TIMESTAMP(6) WITH TIME ZONE")
  private Timestamp upbitEventTime;

  @Column(name = "binance_price", nullable = false, columnDefinition = "DOUBLE PRECISION")
  private double binancePrice;

  @Column(name = "binance_quantity", nullable = false, columnDefinition = "DOUBLE PRECISION")
  private double binanceQuantity;

  @Column(name = "binance_commission", nullable = false, columnDefinition = "REAL")
  private float binanceCommission;

  @Column(
      name = "binance_event_time",
      nullable = false,
      columnDefinition = "TIMESTAMP(6) WITH TIME ZONE")
  private Timestamp binanceEventTime;

  @Column(name = "is_maker", columnDefinition = "BOOLEAN DEFAULT FALSE")
  private boolean isMaker;

  @Column(name = "profit_rate", columnDefinition = "REAL")
  private float profitRate;

  @Column(name = "profit_rate_with_fees", columnDefinition = "REAL")
  private float profitRateWithFees;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP(6) WITH TIME ZONE")
  private Timestamp createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP(6) WITH TIME ZONE")
  private Timestamp updatedAt;

  @Builder
  public SellOrder(
      BuyOrder buyOrder,
      ExchangeRate exchangeRate,
      float premium,
      double upbitPrice,
      double upbitQuantity,
      float upbitCommission,
      String upbitEventTime,
      double binancePrice,
      double binanceQuantity,
      float binanceCommission,
      Long binanceEventTime,
      boolean isMaker,
      float profitRate,
      float profitRateWithFees) {
    this.exchangeRate = exchangeRate;
    this.premium = premium;
    this.upbitPrice = upbitPrice;
    this.upbitQuantity = upbitQuantity;
    this.upbitCommission = upbitCommission;
    this.upbitEventTime =
        Timestamp.valueOf(
            LocalDateTime.parse(
                upbitEventTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")));
    this.binancePrice = binancePrice;
    this.binanceQuantity = binanceQuantity;
    this.binanceCommission = binanceCommission;
    this.binanceEventTime = new Timestamp(binanceEventTime);
    this.isMaker = isMaker;
    this.profitRate = profitRate;
    this.profitRateWithFees = profitRateWithFees;
    this.buyOrder = buyOrder;

    if (buyOrder != null && !buyOrder.getSellOrders().contains(this)) {
      buyOrder.getSellOrders().add(this);
    }
  }
}
