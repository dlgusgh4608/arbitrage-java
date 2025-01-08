package main.arbitrage.infrastructure.exchange.upbit.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import main.arbitrage.infrastructure.exchange.upbit.dto.enums.UpbitOrderEnums;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpbitGetOrderResponse(
    String uuid,
    String side,
    String ordType,
    Double price,
    UpbitOrderEnums.State state,
    String createdAt,
    double volume,
    float paidFee,
    double executedVolume,
    List<Trade> trades) {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public record Trade(Double funds) {}
}
