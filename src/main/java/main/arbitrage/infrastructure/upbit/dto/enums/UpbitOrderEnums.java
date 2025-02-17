package main.arbitrage.infrastructure.upbit.dto.enums;

public class UpbitOrderEnums {
  public enum OrdType {
    limit,
    market,
    price
  }

  public enum Side {
    bid,
    ask
  }

  public enum State {
    wait,
    watch,
    done,
    cancel
  }
}
