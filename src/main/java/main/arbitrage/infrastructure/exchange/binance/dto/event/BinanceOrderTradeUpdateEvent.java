package main.arbitrage.infrastructure.exchange.binance.dto.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class BinanceOrderTradeUpdateEvent {
  private final Long eventTime;
  private final String clientId;
  private final String status;
  private final String side;
  private final String symbol;
  private final Double price;
  private final Double quantity;
  private final Boolean isMaker;
  private final Float commission;

  @Builder
  public BinanceOrderTradeUpdateEvent(
      Long eventTime,
      String clientId,
      String status,
      String side,
      String symbol,
      Double price,
      Double quantity,
      Boolean isMaker,
      Float commission) {
    this.eventTime = eventTime;
    this.clientId = clientId;
    this.status = status;
    this.side = side;
    this.symbol = symbol;
    this.price = price;
    this.quantity = quantity;
    this.isMaker = isMaker;
    this.commission = commission;
  }
}
