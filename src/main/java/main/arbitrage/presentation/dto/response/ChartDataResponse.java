package main.arbitrage.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChartDataResponse {
  private final long x;
  private final float o;
  private final float h;
  private final float l;
  private final float c;
}
