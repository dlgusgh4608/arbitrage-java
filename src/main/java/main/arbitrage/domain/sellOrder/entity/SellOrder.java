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
import java.time.LocalDateTime;
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

  @Column(name = "binance_price", nullable = false, columnDefinition = "DOUBLE PRECISION")
  private double binancePrice;

  @Column(name = "binance_quantity", nullable = false, columnDefinition = "DOUBLE PRECISION")
  private double binanceQuantity;

  @Column(name = "binance_commission", nullable = false, columnDefinition = "REAL")
  private float binanceCommission;

  @Column(name = "is_maker", columnDefinition = "BOOLEAN DEFAULT FALSE")
  private boolean isMaker;

  @Column(name = "profit_rate", columnDefinition = "REAL")
  private float profitRate;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Builder
  public SellOrder(
      BuyOrder buyOrder,
      ExchangeRate exchangeRate,
      float premium,
      double upbitPrice,
      double upbitQuantity,
      float upbitCommission,
      double binancePrice,
      double binanceQuantity,
      float binanceCommission,
      boolean isMaker,
      float profitRate) {
    this.exchangeRate = exchangeRate;
    this.premium = premium;
    this.upbitPrice = upbitPrice;
    this.upbitQuantity = upbitQuantity;
    this.upbitCommission = upbitCommission;
    this.binancePrice = binancePrice;
    this.binanceQuantity = binanceQuantity;
    this.binanceCommission = binanceCommission;
    this.isMaker = isMaker;
    this.profitRate = profitRate;
    this.buyOrder = buyOrder;

    if (buyOrder != null && !buyOrder.getSellOrders().contains(this)) {
      buyOrder.getSellOrders().add(this);
    }
  }
}
