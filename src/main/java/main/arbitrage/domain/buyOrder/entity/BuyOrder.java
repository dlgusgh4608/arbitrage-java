package main.arbitrage.domain.buyOrder.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.sellOrder.entity.SellOrder;
import main.arbitrage.domain.symbol.entity.Symbol;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "buy_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BuyOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

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

  @Column(name = "upbit_quantity", nullable = false, columnDefinition = "DOUBLE PRECISION")
  private double upbitQuantity;

  @Column(name = "upbit_commission", nullable = false, columnDefinition = "REAL")
  private float upbitCommission;

  @Column(
      name = "upbit_event_time",
      nullable = false,
      columnDefinition = "TIMESTAMP(6) WITHOUT TIME ZONE")
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
      columnDefinition = "TIMESTAMP(6) WITHOUT TIME ZONE")
  private Timestamp binanceEventTime;

  @Column(name = "is_maker", columnDefinition = "BOOLEAN DEFAULT FALSE")
  private boolean isMaker;

  @Column(name = "is_close", columnDefinition = "BOOLEAN DEFAULT FALSE")
  private boolean isClose;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Timestamp createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Timestamp updatedAt;

  @OneToMany(mappedBy = "buyOrder", fetch = FetchType.LAZY)
  private List<SellOrder> sellOrders = new ArrayList<>();

  @Builder
  public BuyOrder(
      Long userId,
      Symbol symbol,
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
      boolean isClose) {
    this.userId = userId;
    this.symbol = symbol;
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
    this.isClose = isClose;
  }

  public void close() {
    this.isClose = true;
  }

  public void setSellOrder(List<SellOrder> sellOrders) {
    this.sellOrders = sellOrders;
  }

  public BigDecimal getRestBinanceQty() {
    BigDecimal totalBinanceQty = BigDecimal.valueOf(this.binanceQuantity);
    BigDecimal soldQty =
        sellOrders.stream()
            .map(sellOrder -> BigDecimal.valueOf(sellOrder.getBinanceQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return totalBinanceQty.subtract(soldQty).setScale(8, RoundingMode.HALF_UP);
  }

  public BigDecimal getRestUpbitQty() {
    BigDecimal totalUpbitQty = BigDecimal.valueOf(this.upbitQuantity);
    BigDecimal soldQty =
        sellOrders.stream()
            .map(sellOrder -> BigDecimal.valueOf(sellOrder.getUpbitQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return totalUpbitQty.subtract(soldQty).setScale(8, RoundingMode.HALF_UP);
  }
}
