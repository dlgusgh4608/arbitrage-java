package main.arbitrage.application.order.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;

@Getter
@Builder
@ToString
public class OrderCalcResultDTO {
  private final BuyOrder buyOrder;
  private final BigDecimal binanceQty;
  private final BigDecimal upbitQty;
  private final boolean isClose;
}
