package main.arbitrage.infrastructure.binance.dto.enums;

public class BinanceEnums {
  public enum Type {
    LIMIT,
    MARKET
  }

  public enum TimeInForce {
    IOC,
    GTC,
    GTD
  }

  // https://developers.binance.com/docs/derivatives/usds-margined-futures/user-data-streams/Event-Order-Update
  public enum Status {
    NEW,
    PARTIALLY_FILLED,
    FILLED,
    CANCELED,
    EXPIRED,
    EXPIRED_IN_MATCH
  }

  public enum Side {
    BUY,
    SELL
  }

  public enum MarginType {
    ISOLATED,
    CROSSED
  }

  // https://developers.binance.com/docs/derivatives/usds-margined-futures/user-data-streams/Event-Order-Update
  public enum OrderType {
    LIMIT,
    MARKET,
    STOP,
    STOP_MARKET,
    TAKE_PROFIT,
    TAKE_PROFIT_MARKET,
    TRAILING_STOP_MARKET,
    LIQUIDATION
  }

  public enum ExecutionType {
    NEW,
    CANCELED,
    CALCULATED,
    EXPIRED,
    TRADE,
    AMENDMENT
  }
}
