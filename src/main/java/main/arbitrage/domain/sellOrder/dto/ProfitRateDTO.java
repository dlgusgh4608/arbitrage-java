package main.arbitrage.domain.sellOrder.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProfitRateDTO {
  private final float profitRate;
  private final float profitRateWithFees;
}
